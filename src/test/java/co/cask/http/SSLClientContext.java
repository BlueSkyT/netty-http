/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.http;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Provides Client Context
 * Used by HttpsServerTest
 */
public class SSLClientContext {

  private SSLContext clientContext;
  private String protocol = "TLS";

  public SSLClientContext() {
    this(null, null);
  }

  public SSLClientContext(File keyStore, String keyStorePassword) {

    try {
      KeyManagerFactory kmf = null;
      if (keyStore != null && keyStorePassword != null) {
        KeyStore ks = getKeyStore(keyStore, keyStorePassword);
        kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keyStorePassword.toCharArray());
      }
      clientContext = SSLContext.getInstance(protocol);
      clientContext.init(kmf == null ? null : kmf.getKeyManagers(),
                         InsecureTrustManagerFactory.INSTANCE.getTrustManagers(), null);
    } catch (Exception e) {
      throw Throwables.propagate(new Exception("Failed to initialize the client-side SSLContext", e));
    }
  }

  private static KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
    KeyStore ks = null;
    InputStream is = new FileInputStream(keyStore);
    try {
      ks = KeyStore.getInstance("JKS");
      ks.load(is, keyStorePassword.toCharArray());
    } catch (Exception ex) {
      if (ex instanceof RuntimeException) {
        throw ((RuntimeException) ex);
      }
      throw new IOException(ex);
    } finally {
      Closeables.closeQuietly(is);
    }
    return ks;
  }

  public SSLContext getClientContext() {
    return clientContext;
  }
}
