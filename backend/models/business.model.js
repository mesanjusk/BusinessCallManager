const mongoose = require("mongoose")
const { v4: uuid } = require("uuid")

function generateInviteCode() {
	return Math.random().toString(36).substring(2, 8).toUpperCase()
}

const businessSchema = mongoose.Schema({
	business_uuid: { type: String, default: uuid },
	owner_uuid: { type: String, required: true },
	business_name: { type: String, required: true },
	plan: { type: String, enum: ["free", "premium"], default: "free" },
	subscription_expiry: { type: Date },
	team_members: [
		{
			user_uuid: { type: String },
			role: { type: String, enum: ["owner", "member"] },
			joined_at: { type: Date },
		},
	],
	invite_code: { type: String, default: generateInviteCode },
	created_at: { type: Date, default: Date.now },
})

module.exports = mongoose.model("businesses", businessSchema)
