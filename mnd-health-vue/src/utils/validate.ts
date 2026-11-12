// 验证字符串是否为空
export function stringIsEmpty(s: string | null): boolean {
  return s == null || s.trim().length === 0;
}

// 验证邮箱地址是否合法
export function isEmail(s: string): boolean {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(s);
}

// 验证手机号是否合法（中国大陆）
export function isPhone(s: string): boolean {
  return /^1[3-9]\d{9}$/.test(s);
}

// 验证URL地址是否合法
export function isURL(s: string): boolean {
  try {
    new URL(s);
    return true;
  } catch {
    return false;
  }
}

// 验证用户名（字母数字，5-50位）
export function isUsername(s: string): boolean {
  return /^[a-zA-Z0-9]{5,50}$/.test(s);
}

// 验证密码（字母数字，6-20位）
export function isPassword(s: string): boolean {
  return /^[a-zA-Z0-9]{6,20}$/.test(s);
}

// 验证手机短信验证码（6位数字）
export function isSmsCode(s: string): boolean {
  return /^\d{6}$/.test(s);
}
