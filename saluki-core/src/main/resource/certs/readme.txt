1. 查看OpenSSL版本
openssl version –a

2. Config openssl.cnf
cd /etc/pki/tls

3. 生成RSA密钥的方法
openssl genrsa -des3 -out server.key 2048

这个命令会生成一个2048位的密钥，同时有一个des3方法加密的密码，如果你不想要每次都输入密码，可以改成：

openssl genrsa -out server.key 2048

建议用2048位密钥，少于此可能会不安全或很快将不安全。

 

4. 生成一个证书请求

openssl req –new -x509 -key server.key -out server.pem

这个命令将会生成一个证书请求，当然，用到了前面生成的密钥server.key文件

这里将生成一个新的文件server.pem，即一个证书请求文件，你可以拿着这个文件去数字证书颁发机构（即CA）申请一个数字证书。CA会给你一个新的文件ca.pem，那才是你的数字证书。

 

如果是自己做测试，那么证书的申请机构和颁发机构都是自己。就可以用下面这个命令来生成证书：

openssl req -new -x509 -key server.key -out ca.pem -days 1095

这个命令将用上面生成的密钥server.key生成一个数字证书ca.pem

5. for netty
openssl pkcs8 -topk8 -inform PEM -in server.key -outform PEM -nocrypt -out server_pkcs8.key

6. 查看证书
openssl x509 -in ca.pem -inform pem -noout -text



总结：

========
openssl genrsa -des3 -out server.key 2048
//openssl genrsa -out server.key 2048

openssl req -new -x509 -key server.key -out server.pem
openssl req -new -x509 -key server.key -out ca.pem -days 1095
openssl pkcs8 -topk8 -inform PEM -in server.key -outform PEM -nocrypt -out server_pkcs8.key
