const express = require("express")
const axios = require("axios")
const leadRouter = express.Router()
const leadModel = require("../models/lead.model.js")
const businessModel = require("../models/business.model.js")
const { requirePremium } = require("../middleware/planEnforcement.js")

const WA_URL = `https://graph.facebook.com/${process.env.WHATSAPP_API_VERSION}/${process.env.WHATSAPP_PHONE_NUMBER_ID}/messages`

leadRouter.post("/createLead", async (req, res) => {
	try {
		const { user_uuid, phone, name, source, status, next_follow_up, call_refs } = req.body

		const business = await businessModel.findOne({ "team_members.user_uuid": user_uuid })
		if (!business || business.plan === "free") {
			const leadCount = await leadModel.countDocuments({ user_uuid })
			if (leadCount >= 100) {
				return res.status(403).json({
					message: "Free plan limit reached (100 leads). Upgrade to premium.",
					success: false,
				})
			}
		}

		const lead = await leadModel.create({ user_uuid, phone, name, source, status, next_follow_up, call_refs })
		res.status(200).json({ lead, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

leadRouter.post("/updateLead", async (req, res) => {
	try {
		const { lead_uuid, ...fields } = req.body
		fields.updated_at = new Date()
		const lead = await leadModel.findOneAndUpdate({ lead_uuid }, { $set: fields }, { new: true })
		res.status(200).json({ lead, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

leadRouter.post("/fetchLeads", async (req, res) => {
	try {
		const { user_uuid, status, page = 0, pageSize = 50 } = req.body
		const query = { user_uuid }
		if (status) query.status = status
		const total = page === 0 ? await leadModel.countDocuments(query) : null
		const leads = await leadModel.find(query).sort({ created_at: -1 }).skip(page * pageSize).limit(pageSize)
		res.status(200).json({ leads, total, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

leadRouter.post("/deleteLead", async (req, res) => {
	try {
		const { lead_uuid, user_uuid } = req.body
		await leadModel.findOneAndDelete({ lead_uuid, user_uuid })
		res.status(200).json({ success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

leadRouter.post("/bulkFetch", async (req, res) => {
	try {
		const { user_uuid, since_timestamp } = req.body
		const leads = await leadModel.find({ user_uuid, updated_at: { $gt: new Date(since_timestamp) } })
		res.status(200).json({ leads, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

leadRouter.post("/sendFollowup", requirePremium, async (req, res) => {
	try {
		const { user_uuid, lead_uuid, template_message } = req.body
		const lead = await leadModel.findOne({ lead_uuid })
		if (!lead) return res.status(404).json({ message: "Lead not found", success: false })

		await axios.post(
			WA_URL,
			{
				messaging_product: "whatsapp",
				to: lead.phone,
				type: "text",
				text: { body: template_message },
			},
			{
				headers: {
					Authorization: `Bearer ${process.env.WHATSAPP_ACCESS_TOKEN}`,
					"Content-Type": "application/json",
				},
			}
		)

		res.status(200).json({ success: true, message: "Message sent" })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

module.exports = leadRouter
