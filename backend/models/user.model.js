const mongoose = require("mongoose");
const { v4: uuid } = require("uuid");

const userSchema = mongoose.Schema({
  user_mobile: { type: String },
  user_uuid: { type: String,default: uuid},
  business_uuid: { type: String, default: null },
  role: { type: String, default: "owner" },
  created_at: { type: Date, default: Date.now },
});

module.exports = mongoose.model("users", userSchema);
