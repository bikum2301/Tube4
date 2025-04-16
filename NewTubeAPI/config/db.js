const mongoose = require('mongoose');
const dotenv = require('dotenv');

dotenv.config(); // Load biến môi trường từ .env

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGO_URI, {
      // Các options này không còn cần thiết ở Mongoose v6+
      // useNewUrlParser: true,
      // useUnifiedTopology: true,
      // useCreateIndex: true, // không còn hỗ trợ
      // useFindAndModify: false // không còn hỗ trợ
    });

    console.log(`MongoDB Connected: ${conn.connection.host}`);
  } catch (error) {
    console.error(`Error connecting to MongoDB: ${error.message}`);
    process.exit(1); // Thoát khỏi tiến trình nếu không kết nối được DB
  }
};

module.exports = connectDB;