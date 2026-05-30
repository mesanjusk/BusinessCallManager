const mongoose = require("mongoose")

const appLogSchema = mongoose.Schema({
	logs: { type: String },
	user_uuid: { type: String, required: true, unique: true },
})

module.exports = mongoose.model("app_logs", appLogSchema)
