const express = require("express");
const secondaryContactRouter = express.Router();
const secondaryContactModel = require("../models/secondaryContact.model.js");
const userModel = require("../models/user.model.js");
const callLogModel = require("../models/callLog.model.js");

secondaryContactRouter.post("/addSecondaryContact", async (req, res) => {
  try {
    const body = req.body;
    let result = [];
    for (let value of body) {
      let {
        user_uuid,
        contact_title,
        contact_mobile,
        created_at = new Date().getTime(),
        contact_uuid,
      } = value;
      if (!user_uuid || !contact_title || !contact_mobile || !contact_uuid)
        return res
          .status(400)
          .json({ message: "All fields are required", success: false });

      let user = await userModel.findOne({ user_uuid });
      if (!user) {
        result.push({
          contact_uuid,
          success: false,
          message: "User not found",
        });
        continue;
      }
      let contact = await secondaryContactModel.findOne({
        user_uuid,
        contact_uuid,
        contact_mobile,
        created_at,
      });
      if (contact) {
        result.push({
          contact_uuid,
          success: true,
          message: "Contact already Exist",
        });

        continue;
      }
      let data = await secondaryContactModel.create({
        user_uuid,
        contact_title,
        contact_mobile,
        contact_uuid,
        created_at,
      });
      if (data)
        result.push({
          contact_uuid: data.contact_uuid,
          success: true,
          message: "Contact added successfully",
        });
      else
        result.push({
          contact_uuid,
          success: false,
          message: "Failed to add contact",
        });
    }
    res.status(200).json({
      message: `${
        result.filter((a) => a.success).length
      } contacts added successfully`,
      success: true,
      result,
    });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});
secondaryContactRouter.post("/updateSecondaryContact", async (req, res) => {
  try {
    const body = req.body;
    let result = [];
    for (let value of body) {
      let {
        user_uuid,
        contact_title,
        contact_mobile,
        created_at = new Date().getTime(),
        contact_uuid,
      } = value;
      if (!user_uuid || !contact_title || !contact_mobile || !contact_uuid)
        return res
          .status(400)
          .json({ message: "All fields are required", success: false });

      let user = await userModel.findOne({ user_uuid });
      if (!user) {
        result.push({
          contact_uuid,
          success: false,
          message: "User not found",
        });
        continue;
      }
      let contact = await secondaryContactModel.findOne({
        user_uuid,
        contact_uuid,
      });
      if (contact) {
        let data = await secondaryContactModel.updateOne(
          { user_uuid, contact_uuid },
          { contact_title, contact_mobile, created_at }
        );
        if (data)
          result.push({
            contact_uuid,
            success: true,
            message: "Contact updated successfully",
          });
        else
          result.push({
            contact_uuid,
            success: false,
            message: "Failed to update contact",
          });
        continue;
      }

      result.push({
        contact_uuid,
        success: false,
        message: "Failed to update contact",
      });
    }
    res.status(200).json({
      message: `${
        result.filter((a) => a.success).length
      } contacts Updated successfully`,
      success: true,
      result,
    });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

secondaryContactRouter.post("/deleteSecondaryContact", async (req, res) => {
  try {
    const body = req.body;
    let count = 0;
    let succeed = [];
    let failed = [];
    for (let value of body) {
      const { contact_uuid, user_uuid } = value;
      if (!contact_uuid || !user_uuid)
        return res
          .status(400)
          .json({ message: "All fields are required", success: false });
      let contact = await secondaryContactModel.findOne({
        contact_uuid,
        user_uuid,
      });
      if (!contact) {
        failed.push(value.contact_uuid);
        continue;
      }
      await secondaryContactModel.deleteOne({ contact_uuid, user_uuid });
      succeed.push(value.contact_uuid);
      count++;
    }
    res.status(200).json({
      message: `${count} contacts deleted successfully`,
      success: true,
      succeed,
      failed,
    });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

//fetchAllSecondaryContacts with user_uuid
secondaryContactRouter.post("/fetchAllSecondaryContacts", async (req, res) => {
  try {
    const { user_uuid } = req.body;
    if (!user_uuid)
      return res
        .status(400)
        .json({ message: "All fields are required", success: false });
    let contacts = await secondaryContactModel.find({ user_uuid });
    res.status(200).json({
      contacts,
      success: true,
      message: "Contacts fetched successfully",
    });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

//delete user and its data
secondaryContactRouter.get("/deleteUser", async (req, res) => {
  try {
    const { user_mobile } = req.query;
    if (!user_mobile)
      return res
        .status(200)
        .json({ message: "All fields are required", success: false });
    let userData = await userModel.findOne({ user_mobile:'+'+user_mobile });
    if (!userData)
      return res
        .status(200)
        .json({ message: "User Not Found", success: false });
    const  user_uuid  = userData?.user_uuid;
    await secondaryContactModel.deleteMany({ user_uuid });
    await userModel.deleteOne({ user_uuid });
    await callLogModel.deleteMany({ user_uuid });
    res.status(200).json({
      success: true,
      message: "User deleted successfully",
    });
  } catch (error) {
    console.log(error);
    res.status(200).json({ message: "Internal server error", success: false });
  }
});

module.exports = secondaryContactRouter;
