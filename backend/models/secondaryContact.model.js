const mongoose = require("mongoose");

const secondaryContactSchema = mongoose.Schema({
    user_uuid: { type: String},
    contact_title: { type: String},
    contact_mobile: { type: String},
    created_at: { type: Date },
    contact_uuid: { type: String},
});

module.exports = mongoose.model("secondary_contacts", secondaryContactSchema);