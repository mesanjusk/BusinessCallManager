require("./config/mongo.js")()
const cors = require("cors")
const express = require("express")
const morgan = require("morgan")
const bodyParser = require("body-parser")
const otpRouter = require("./routes/otp.routes.js")
const callLogRouter = require("./routes/callLog.routes.js")
const secondaryContactRouter = require("./routes/secondaryContact.routes.js")
const appLogRouter = require("./routes/appLog.routes.js")
const leadRouter = require("./routes/lead.routes.js")
const businessRouter = require("./routes/business.routes.js")
const taskRouter = require("./routes/task.routes.js")
const reportsRouter = require("./routes/reports.routes.js")
const subscriptionRouter = require("./routes/subscription.routes.js")

const app = express()
app.use(
	cors({
		origin: "*",
		credentials: true
	})
)

app.use(bodyParser.json({ limit: "100mb" }))
app.use(
	bodyParser.urlencoded({
		limit: "100mb",
		extended: true,
		parameterLimit: 50000
	})
)

app.use(morgan("dev"))
app.use("/otp", otpRouter)
app.use("/secondaryContacts", secondaryContactRouter)
app.use("/logs", callLogRouter)
app.use("/appLogs", appLogRouter)
app.use("/leads", leadRouter)
app.use("/business", businessRouter)
app.use("/tasks", taskRouter)
app.use("/reports", reportsRouter)
app.use("/subscription", subscriptionRouter)

app.post("/DeleteMyData", async (req, res) => {
	try {
		return res.status(200).json({ message: "Data Deleted", success: true })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: "Internal server error", success: false })
	}
})
//make views folder static with links like we have terms and conditions file for /TAC
app.use(express.static("views"))
app.get("/TnC", (req, res) => {
	//send the file TermsAndCondition.html which is in views folderz
	res.sendFile("TermsAndConditions.html", { root: __dirname + "/views" })
})
app.get("/Privacy_Policy", (req, res) => {
	//send the file TermsAndCondition.html which is in views folder
	res.sendFile("PrivcyPolicy.html", { root: __dirname + "/views" })
})

module.exports = app
