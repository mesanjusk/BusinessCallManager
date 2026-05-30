const express = require("express")
const logModel = require("../models/appLog.model")
const appLogRouter = express.Router()

appLogRouter.post("/", async (req, res) => {
	try {
		let log = await logModel.findOne({ user_uuid: req.body.user_uuid })
		if (log) {
			delete req.body._id
			log = await logModel.findByIdAndUpdate(log._id, req.body, { new: true })
			res.json(log)
		} else {
			const newLog = new logModel(req.body)
			const savedLog = await newLog.save()
			res.status(201).json(savedLog)
		}
	} catch (err) {
		res.status(400).json({ message: err.message })
	}
})

appLogRouter.get("/", async (req, res) => {
	try {
		const logs = await logModel.find()
		res.json(logs)
	} catch (err) {
		res.status(500).json({ message: err.message })
	}
})

appLogRouter.get("/:user_uuid", async (req, res) => {
	try {
		const log = await logModel.findOne({ user_uuid: req.params.user_uuid })
		if (!log) return res.status(404).json({ message: "Log not found" })
		res.json(log)
	} catch (err) {
		res.status(500).json({ message: err.message })
	}
})

appLogRouter.delete("/:user_uuid", async (req, res) => {
	try {
		const deletedLog = await logModel.deleteOne({ user_uuid: req.params.user_uuid })
		if (!deletedLog) return res.status(404).json({ message: "Log not found" })
		res.json({ message: "Log deleted" })
	} catch (err) {
		res.status(500).json({ message: err.message })
	}
})

module.exports = appLogRouter
