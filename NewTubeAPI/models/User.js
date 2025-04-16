const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: [true, 'Vui lòng nhập tên người dùng'],
    unique: true,
    trim: true,
  },
  email: {
    type: String,
    required: [true, 'Vui lòng nhập email'],
    unique: true,
    lowercase: true,
    match: [ // Regex kiểm tra định dạng email đơn giản
      /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
      'Vui lòng nhập địa chỉ email hợp lệ',
    ],
  },
  password: {
    type: String,
    required: [true, 'Vui lòng nhập mật khẩu'],
    minlength: 6, // Độ dài tối thiểu
    select: false, // Không tự động trả về password khi query user
  },
}, { timestamps: true }); // Tự động thêm createdAt và updatedAt

// Middleware: Hash password trước khi lưu
userSchema.pre('save', async function (next) {
  // Chỉ hash password nếu nó được sửa đổi (hoặc là mới)
  if (!this.isModified('password')) {
    return next();
  }
  // Hash password với salt round là 10
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
  next();
});

// Method: So sánh mật khẩu nhập vào với mật khẩu đã hash trong DB
userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

module.exports = mongoose.model('User', userSchema);