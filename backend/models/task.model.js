const mongoose = require("mongoose")
const { v4: uuid } = require("uuid")

const taskSchema = mongoose.Schema({
	task_uuid: { type: String, default: uuid },
	business_uuid: { type: String },
	created_by_uuid: { type: String, required: true },
	assigned_to_uuid: { type: String, required: true },
	lead_uuid: { type: String },
	call_ref: { type: String },
	title: { type: String, required: true },
	description: { type: String },
	due_date: { type: Date },
	priority: { type: String, enum: ["Low", "Medium", "High"], default: "Medium" },
	status: { type: String, enum: ["Pending", "In Progress", "Done", "Overdue"], default: "Pending" },
	created_at: { type: Date, default: Date.now },
	updated_at: { type: Date, default: Date.now },
})

module.exports = mongoose.model("tasks", taskSchema)
