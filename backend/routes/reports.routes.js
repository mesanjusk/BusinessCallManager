const express = require("express")
const reportsRouter = express.Router()
const callLogModel = require("../models/callLog.model.js")
const leadModel = require("../models/lead.model.js")
const taskModel = require("../models/task.model.js")
const businessModel = require("../models/business.model.js")
const { requirePremium } = require("../middleware/planEnforcement.js")

reportsRouter.post("/summary", async (req, res) => {
	try {
		const { user_uuid, from_date, to_date } = req.body
		const from = new Date(from_date)
		const to = new Date(to_date)

		const calls_made = await callLogModel.countDocuments({ user_uuid, date: { $gte: from.getTime().toString(), $lte: to.getTime().toString() } })
		const leads_added = await leadModel.countDocuments({ user_uuid, created_at: { $gte: from, $lte: to } })
		const leads_won = await leadModel.countDocuments({ user_uuid, status: "Won", updated_at: { $gte: from, $lte: to } })
		const leads_lost = await leadModel.countDocuments({ user_uuid, status: "Lost", updated_at: { $gte: from, $lte: to } })
		const conversion_rate = leads_added > 0 ? (leads_won / leads_added) * 100 : 0

		res.status(200).json({ calls_made, leads_added, leads_won, leads_lost, conversion_rate, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

reportsRouter.post("/memberActivity", async (req, res) => {
	try {
		const { owner_uuid, from_date, to_date } = req.body
		const from = new Date(from_date)
		const to = new Date(to_date)

		const business = await businessModel.findOne({ owner_uuid })
		if (!business) return res.status(404).json({ message: "Business not found", success: false })

		const activity = await Promise.all(
			business.team_members.map(async (member) => {
				const calls = await callLogModel.countDocuments({ user_uuid: member.user_uuid, date: { $gte: from.getTime().toString(), $lte: to.getTime().toString() } })
				const leads = await leadModel.countDocuments({ user_uuid: member.user_uuid, created_at: { $gte: from, $lte: to } })
				const tasks_done = await taskModel.countDocuments({ assigned_to_uuid: member.user_uuid, status: "Done", updated_at: { $gte: from, $lte: to } })
				return { user_uuid: member.user_uuid, calls, leads, tasks_done }
			})
		)

		res.status(200).json({ activity, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

reportsRouter.post("/exportCsv", requirePremium, async (req, res) => {
	try {
		const { user_uuid } = req.body
		const leads = await leadModel.find({ user_uuid })
		res.status(200).json({ leads, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

module.exports = reportsRouter
