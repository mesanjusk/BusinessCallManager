const businessModel = require("../models/business.model.js")

module.exports.requirePremium = async (req, res, next) => {
	try {
		const { user_uuid } = req.body
		const business = await businessModel.findOne({
			"team_members.user_uuid": user_uuid,
		})
		if (!business) return next()
		if (business.plan !== "premium") {
			return res.status(403).json({ message: "Premium plan required", success: false })
		}
		next()
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
}
