const express = require("express")
const axios = require("axios")
const otpRouter = express.Router()
const Users = require("../models/user.model.js")

otpRouter.post("/verify_token", async (req, res) => {
	try {
		const { token } = req.body
		if (!token)
			return res.status(400).json({
				message: "Token is required for verification",
				success: false
			})

		const response = await axios({
			method: "POST",
			url: "https://control.msg91.com/api/v5/widget/verifyAccessToken",
			headers: {
				"Content-Type": "application/json",
				"Accept": "application/json"
			},
			data: {
				"authkey": process.env.MSG91_AUTH_KEY,
				"access-token": token
			}
		})

		if (response.data?.type === "success") {
			const userMobile = "+91" + response.data?.message?.trim()?.slice(-10)
			let user = await Users.findOne({ user_mobile: userMobile })
			if (!user) user = await Users.create({ user_mobile: userMobile })

			return res.json({
				success: true,
				verificationId: response.data?.message,
				user_uuid: user.user_uuid
			})
		} else if ([701, 702].includes(response.data.code))
			return res.status(401).json({
				message: "Invalid token, relogin required.",
				success: false
			})
		else {
			return res.status(503).json({
				message: "OTP verification service unavailable. Please contact support.",
				success: false
			})
		}
	} catch (error) {
		res.status(500).json({
			message: "Internal server error",
			success: false
		})
	}
})

module.exports = otpRouter
