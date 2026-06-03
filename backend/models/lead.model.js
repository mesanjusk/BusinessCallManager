const mongoose = require("mongoose")
const { v4: uuid } = require("uuid")

const leadSchema = mongoose.Schema({
	lead_uuid: { type: String, default: uuid },
	business_uuid: { type: String },
	user_uuid: { type: String, required: true },
	phone: { type: String, required: true },
	name: { type: String },
	source: { type: String, enum: ["call", "manual"], default: "call" },
	status: { type: String, enum: ["New", "Contacted", "Interested", "Negotiation", "Won", "Lost"], default: "New" },
	notes: [{ text: { type: String }, created_at: { type: Date } }],
	next_follow_up: { type: Date },
	call_refs: [{ type: String }],
	created_at: { type: Date, default: Date.now },
	updated_at: { type: Date, default: Date.now },
})

module.exports = mongoose.model("leads", leadSchema)
