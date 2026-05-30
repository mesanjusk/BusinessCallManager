const mongoose = require("mongoose")

const callLogSchema = mongoose.Schema({
	cachedName: { type: String },
	callNote: { type: String },
	callerId: { type: String },
	colorCode: { type: String },
	date: { type: String },
	duration: { type: String },
	id: { type: String },
	number: { type: String },
	type: { type: String },
	user_uuid: { type: String },
	tasks: [
		{
			status: { type: Number },
			title: { type: String },
		},
	],
})

module.exports = mongoose.model("call_logs", callLogSchema)