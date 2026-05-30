const express = require("express")
const axios = require("axios")
const otpRouter = express.Router()
const Users = require("../models/user.model.js")
const OtpRecord = require("../models/otp.model.js")

const WA_URL = `https://graph.facebook.com/${process.env.WHATSAPP_API_VERSION}/${process.env.WHATSAPP_PHONE_NUMBER_ID}/messages`
const OTP_EXPIRY_MINUTES = 5

function generateOtp() {
	return Math.floor(100000 + Math.random() * 900000).toString()
}

// POST /otp/sendOtp
// Body: { phone: "919876543210" }  — country code + number, no +
otpRouter.post("/sendOtp", async (req, res) => {
	try {
		let { phone } = req.body
		if (!phone)
			return res.status(400).json({ message: "Phone number is required", success: false })

		// Normalise: strip leading + if present, ensure starts with country code
		phone = phone.replace(/^\+/, "")

		const otp = generateOtp()
		const expires_at = new Date(Date.now() + OTP_EXPIRY_MINUTES * 60 * 1000)

		// Invalidate any previous unused OTPs for this number
		await OtpRecord.deleteMany({ phone, verified: false })
		await OtpRecord.create({ phone, otp, expires_at })

		// Send OTP via WhatsApp Cloud API using the pre-approved otp template
		await axios.post(
			WA_URL,
			{
				messaging_product: "whatsapp",
				to: phone,
				type: "template",
				template: {
					name: "instify_otp",
					language: { code: "en_US" },
					components: [
						{
							type: "body",
							parameters: [{ type: "text", text: otp }]
						}
					]
				}
			},
			{
				headers: {
					Authorization: `Bearer ${process.env.WHATSAPP_ACCESS_TOKEN}`,
					"Content-Type": "application/json"
				}
			}
		)

		return res.json({ success: true, message: "OTP sent via WhatsApp" })
	} catch (error) {
		const waError = error?.response?.data?.error
		console.error("sendOtp error:", waError || error.message)
		const userMsg = waError
			? `WhatsApp error: ${waError.message || waError.type || JSON.stringify(waError)}`
			: "Failed to send OTP. Please try again."
		res.status(400).json({ message: userMsg, success: false })
	}
})

// POST /otp/verify_token
// Body: { phone: "919876543210", otp: "123456" }
otpRouter.post("/verify_token", async (req, res) => {
	try {
		let { phone, otp } = req.body
		if (!phone || !otp)
			return res.status(400).json({ message: "Phone and OTP are required", success: false })

		phone = phone.replace(/^\+/, "")

		const record = await OtpRecord.findOne({ phone, verified: false }).sort({ expires_at: -1 })

		if (!record)
			return res.status(401).json({ message: "OTP not found or already used. Please request a new OTP.", success: false })

		if (new Date() > record.expires_at)
			return res.status(401).json({ message: "OTP expired. Please request a new one.", success: false })

		if (record.otp !== otp.toString())
			return res.status(401).json({ message: "Invalid OTP.", success: false })

		record.verified = true
		await record.save()

		const userMobile = "+" + phone
		let user = await Users.findOne({ user_mobile: userMobile })
		if (!user) user = await Users.create({ user_mobile: userMobile })

		return res.json({
			success: true,
			verificationId: phone,
			user_uuid: user.user_uuid
		})
	} catch (error) {
		console.error("verify_token error:", error.message)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

module.exports = otpRouter
