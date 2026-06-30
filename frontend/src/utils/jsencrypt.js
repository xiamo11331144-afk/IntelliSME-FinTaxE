import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'

// 公钥由后端配置，部署时替换为真实公钥
const publicKey = import.meta.env.VITE_RSA_PUBLIC_KEY || ''

// 加密
export function encrypt(txt) {
  if (!publicKey) {
    console.warn('RSA 公钥未配置，请在 .env 中设置 VITE_RSA_PUBLIC_KEY')
    return txt
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  return encryptor.encrypt(txt)
}