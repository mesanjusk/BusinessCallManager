const express = require("express")
const subscriptionRouter = express.Router()
const businessModel = require("../models/business.model.js")

subscriptionRouter.post("/upgrade", async (req, res) => {
	try {
		const { user_uuid } = req.body
		const subscription_expiry = new Date()
		subscription_expiry.setFullYear(subscription_expiry.getFullYear() + 1)

		const business = await businessModel.findOneAndUpdate(
			{ "team_members.user_uuid": user_uuid },
			{ $set: { plan: "premium", subscription_expiry } },
			{ new: true }
		)
		if (!business) return res.status(404).json({ message: "Business not found", success: false })
		res.status(200).json({ business, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

subscriptionRouter.get("/status", async (req, res) => {
	try {
		const { user_uuid } = req.query
		const business = await businessModel.findOne({ "team_members.user_uuid": user_uuid })
		const plan = business ? business.plan : "free"
		const subscription_expiry = business ? business.subscription_expiry : null
		res.status(200).json({
			plan,
			subscription_expiry,
			limits: { max_leads: plan === "premium" ? null : 100 },
			success: true,
		})
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

module.exports = subscriptionRouter
