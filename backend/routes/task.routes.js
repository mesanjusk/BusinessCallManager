const express = require("express")
const taskRouter = express.Router()
const taskModel = require("../models/task.model.js")
const businessModel = require("../models/business.model.js")

taskRouter.post("/createTask", async (req, res) => {
	try {
		const { created_by_uuid, assigned_to_uuid, business_uuid, lead_uuid, call_ref, title, description, due_date, priority } = req.body
		const task = await taskModel.create({ created_by_uuid, assigned_to_uuid, business_uuid, lead_uuid, call_ref, title, description, due_date, priority })
		res.status(200).json({ task, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

taskRouter.post("/updateTask", async (req, res) => {
	try {
		const { task_uuid, ...fields } = req.body
		fields.updated_at = new Date()
		const task = await taskModel.findOneAndUpdate({ task_uuid }, { $set: fields }, { new: true })
		res.status(200).json({ task, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

taskRouter.post("/fetchMyTasks", async (req, res) => {
	try {
		const { user_uuid, status } = req.body
		const query = { assigned_to_uuid: user_uuid }
		if (status) query.status = status
		const tasks = await taskModel.find(query).sort({ created_at: -1 })
		res.status(200).json({ tasks, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

taskRouter.post("/fetchTeamTasks", async (req, res) => {
	try {
		const { owner_uuid } = req.body
		const business = await businessModel.findOne({ owner_uuid })
		if (!business) return res.status(404).json({ message: "Business not found", success: false })
		const tasks = await taskModel.find({ business_uuid: business.business_uuid }).sort({ created_at: -1 })
		res.status(200).json({ tasks, success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

taskRouter.post("/deleteTask", async (req, res) => {
	try {
		const { task_uuid, user_uuid } = req.body
		await taskModel.findOneAndDelete({ task_uuid, $or: [{ created_by_uuid: user_uuid }, { assigned_to_uuid: user_uuid }] })
		res.status(200).json({ success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})

module.exports = taskRouter
