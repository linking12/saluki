/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

/**
 * @author shimingliu 2016年12月14日 下午10:27:04
 * @version SslUtil.java, v 0.0.1 2016年12月14日 下午10:27:04 shimingliu
 */
public final class SslUtil {

    private SslUtil(){
    }

    public static File loadFileCert(String name) throws IOException {
        InputStream in = SslUtil.class.getClassLoader().getResourceAsStream("certs/" + name);
        File tmpFile = File.createTempFile(name, "");
        tmpFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
        try {
            int b;
            while ((b = in.read()) != -1) {
                writer.write(b);
            }
        } finally {
            writer.close();
        }

        return tmpFile;
    }

    public static InputStream loadInputStreamCert(String name) {
        return SslUtil.class.getClassLoader().getResourceAsStream("certs/" + name);
    }

    public static X509Certificate loadX509Cert(String fileName) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream in = SslUtil.class.getClassLoader().getResourceAsStream("certs/" + fileName);
        try {
            return (X509Certificate) cf.generateCertificate(in);
        } finally {
            in.close();
        }
    }

    public static SSLSocketFactory newSslSocketFactoryForCa(File certChainFile) throws Exception {
        InputStream is = new FileInputStream(certChainFile);
        try {
            return newSslSocketFactoryForCa(is);
        } finally {
            is.close();
        }
    }

    public static SSLSocketFactory newSslSocketFactoryForCa(InputStream certChain) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new BufferedInputStream(certChain));
        X500Principal principal = cert.getSubjectX500Principal();
        ks.setCertificateEntry(principal.getName("RFC2253"), cert);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagerFactory.getTrustManagers(), null);
        return context.getSocketFactory();
    }
}
