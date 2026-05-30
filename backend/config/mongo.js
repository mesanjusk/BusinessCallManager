const mongoose = require("mongoose")

const connectDB = async () => {
	const conn = await mongoose.connect(process.env.MONGO_URI, {
		connectTimeoutMS: 10000,
		socketTimeoutMS: 10000
	})
	console.green(`Connected to MongoDB Atlas ${conn.connection.host}`)
}

module.exports = connectDB
