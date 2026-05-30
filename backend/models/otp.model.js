const mongoose = require("mongoose")

const otpSchema = mongoose.Schema({
	phone: { type: String, required: true },
	otp: { type: String, required: true },
	expires_at: { type: Date, required: true },
	verified: { type: Boolean, default: false }
})

otpSchema.index({ expires_at: 1 }, { expireAfterSeconds: 0 })

module.exports = mongoose.model("otps", otpSchema)
