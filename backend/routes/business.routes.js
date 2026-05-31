const express = require("express")
const businessRouter = express.Router()
const businessModel = require("../models/business.model.js")
const userModel = require("../models/user.model.js")
const callLogModel = require("../models/callLog.model.js")
const leadModel = require("../models/lead.model.js")

businessRouter.post("/create", async (req, res) => {
	try {
		const { user_uuid, business_name } = req.body
		const business = await businessModel.create({
			owner_uuid: user_uuid,
			business_name,
			team_members: [{ user_uuid, role: "owner", joined_at: new Date() }],
		})
		await userModel.findOneAndUpdate({ user_uuid }, { $set: { business_uuid: business.business_uuid } })
		res.status(200).json({ business, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

businessRouter.post("/join", async (req, res) => {
	try {
		const { user_uuid, invite_code } = req.body
		const business = await businessModel.findOneAndUpdate(
			{ invite_code },
			{ $push: { team_members: { user_uuid, role: "member", joined_at: new Date() } } },
			{ new: true }
		)
		if (!business) return res.status(404).json({ message: "Business not found", success: false })
		await userModel.findOneAndUpdate({ user_uuid }, { $set: { business_uuid: business.business_uuid } })
		res.status(200).json({ business, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

businessRouter.post("/getTeam", async (req, res) => {
	try {
		const { user_uuid } = req.body
		const business = await businessModel.findOne({ "team_members.user_uuid": user_uuid })
		if (!business) return res.status(404).json({ message: "Business not found", success: false })

		const members = await Promise.all(
			business.team_members.map(async (member) => {
				const calls = await callLogModel.countDocuments({ user_uuid: member.user_uuid })
				const leads = await leadModel.countDocuments({ user_uuid: member.user_uuid })
				return { ...member.toObject(), calls, leads }
			})
		)

		res.status(200).json({ members, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

businessRouter.post("/removeMember", async (req, res) => {
	try {
		const { owner_uuid, member_uuid } = req.body
		const business = await businessModel.findOne({ owner_uuid })
		if (!business) return res.status(404).json({ message: "Business not found", success: false })

		const requester = business.team_members.find((m) => m.user_uuid === owner_uuid && m.role === "owner")
		if (!requester) return res.status(403).json({ message: "Only owner can remove members", success: false })

		await businessModel.findOneAndUpdate(
			{ owner_uuid },
			{ $pull: { team_members: { user_uuid: member_uuid } } }
		)
		await userModel.findOneAndUpdate({ user_uuid: member_uuid }, { $set: { business_uuid: null } })
		res.status(200).json({ success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

businessRouter.post("/getMyBusiness", async (req, res) => {
	try {
		const { user_uuid } = req.body
		const business = await businessModel.findOne({ "team_members.user_uuid": user_uuid })
		if (!business) return res.status(404).json({ message: "Business not found", success: false })
		res.status(200).json({ business, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

module.exports = businessRouter
