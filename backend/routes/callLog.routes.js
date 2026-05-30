const express = require("express");
const callLogRouter = express.Router();
const callLogModel = require("../models/callLog.model.js")
const userModel = require("../models/user.model.js")

callLogRouter.post("/SyncLogs", async (req, res) => {
  try {
    const body = req.body;
    let result = [];
    for (let value of body) {
      let {
        cachedName,
        callNote,
        callerId,
        colorCode,
        date = new Date().getTime(),
        duration,
        id,
        number,
        type,
        user_uuid,
        tasks,
      } = value;
      if (!user_uuid) {
        result.push({
          number,
          success: false,
          message: "user_uuid is required",
        });
        continue;
      }
      let user = await userModel.findOne({ user_uuid });
      if (!user)
        result.push({
          number,
          success: false,
          message: "User not found",
        });
      let dataExist = await callLogModel.findOne({ number, id, duration, user_uuid });
      if (dataExist) {
        result.push({
          number,
          success: false,
          message: "Log already exist",
        });
        continue;
      }

      let data = await callLogModel.create({
        cachedName,
        callNote,
        callerId,
        colorCode,
        date,
        duration,
        id,
        number,
        type,
        user_uuid,
        tasks,
      });
      if (data)
        result.push({
          number,
          success: true,
          message: "Log added successfully",
        });
      else
        result.push({
          number,
          success: false,
          message: "Failed to add log",
        });
    }
    res.status(200).json({ result, success: true });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

callLogRouter.post("/updateLogs", async (req, res) => {
  try {
    const body = req.body;
    let result = [];
    for (let value of body) {
      let {
        cachedName,
        callNote,
        callerId,
        colorCode,
        date = new Date().getTime(),
        duration,
        id,
        number,
        type,
        user_uuid,
        tasks,
      } = value;
      if (!user_uuid) {
        result.push({
          number,
          success: false,
          message: "user_uuid is required",
        });
        continue;
      }
      let user = await userModel.findOne({ user_uuid });
      if (!user)
        result.push({
          number,
          success: false,
          message: "User not found",
        });
      let dataExist = await callLogModel.findOne({ number, id, duration, user_uuid });
      if (!dataExist) {
        let data = await callLogModel.create({
          cachedName,
          callNote,
          callerId,
          colorCode,
          date,
          duration,
          id,
          number,
          type,
          user_uuid,
          tasks,
        });
        if (data)
          result.push({
            number,
            success: true,
            message: "Log added successfully",
          });
        else
          result.push({
            number,
            success: false,
            message: "Failed to add log",
          });
        continue;
      }

      let data = await callLogModel.updateOne(
        { number, id, duration, user_uuid },
        {
          cachedName,
          callNote,
          callerId,
          colorCode,
          date,
          duration,
          id,
          number,
          type,
          user_uuid,
          tasks,
        }
      );
      if (data)
        result.push({
          number,
          success: true,
          message: "Log Updated successfully",
        });
      else
        result.push({
          number,
          success: false,
          message: "Failed to add log",
        });
    }
    res.status(200).json({ result, success: true });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

//get all logs
callLogRouter.post("/getAllLogs", async (req, res) => {
  try {
    let { user_uuid, to_date, from_date } = req.body;
    if (!user_uuid)
      return res
        .status(400)
        .json({ message: "user_uuid is required", success: false });

    let to = new Date(+to_date).getTime();
    let from = new Date(+from_date).getTime();

    let logs = await callLogModel.find({ user_uuid, date: { $gte: from, $lte: to } });
    logs = logs.sort((a, b) => b.date - a.date);

    if (!logs.length)
      return res.status(404).json({ message: "No logs found", success: false });
    res.status(200).json({ logs, success: true });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

callLogRouter.post("/getAllActiveTaskLogs", async (req, res) => {
  try {
    let { user_uuid } = req.body;
    if (!user_uuid)
      return res
        .status(400)
        .json({ message: "user_uuid is required", success: false });

    let logs = await callLogModel.find({
      user_uuid,
      //last 90 days

      $or: [
        { "tasks.status": 1 },
        {
          //callNote not ""
          callNote: { $ne: "" },
          date: { $gte: new Date().getTime() - 7776000000 },
        },
      ],
    });
    logs = logs.sort((a, b) => b.date - a.date);

    if (!logs.length)
      return res.status(404).json({ message: "No logs found", success: false });
    res.status(200).json({ logs, success: true });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

callLogRouter.post("/userLogs", async (req, res) => {
  try {
    const { user_uuid, user_mobile, date, page=0 } = req.body;

    if (!user_uuid || !user_mobile)
      return res.status(400).json({ message: "Please provide user mobile and uuid", success: false });
    
    if (!userModel.exists({user_uuid,user_mobile})) 
      return res.status(404).json({ message: "User not found", success: false });
    
    const pageSize = 100
    const matchQuery = {
      user_uuid,
      date: {
        $gt: (new Date(parseInt(date)).setUTCHours(0,0,0,0)).toString(),
        $lt: (new Date(parseInt(date)).setUTCHours(24,0,0,0)).toString()
      }
    }

    const pipeline = [
      { $match: matchQuery },
      { $sort: { date: -1 }},
      { $skip: page * pageSize },
      { $limit: pageSize },
    ]

    const total = page === 0 ? await callLogModel.countDocuments(matchQuery) : null
    const logs = total === 0 ? [] : await callLogModel.aggregate(pipeline)

    if (logs.length === 0)
      return res.status(404).json({ message: "No logs found", success: false });

    res.status(200).json({ logs, total, success: true });
  } catch (error) {
    console.error(error)
    res.status(500).json({ message: "Internal server error", success: false });
  }
});

module.exports = callLogRouter;
