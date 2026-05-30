const mongoose = require("mongoose");
const { v4: uuid } = require("uuid");

const userSchema = mongoose.Schema({
  user_mobile: { type: String },
  user_uuid: { type: String,default: uuid},
  created_at: { type: Date, default: Date.now },
});

module.exports = mongoose.model("users", userSchema);
