/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.smb;

import java.io.IOException;
import java.io.InputStream;

import com.hierynomus.smbj.share.File;
import org.apache.camel.Exchange;
import org.apache.camel.WrappedFile;

public class SmbFile implements WrappedFile<File> {

    private final File file;

    public SmbFile(File file) {
        this.file = file;
    }

    void populateHeaders(Exchange exchange) {
        exchange.getMessage().setHeader(SmbConstants.SMB_FILE_PATH, file.getPath());
        exchange.getMessage().setHeader(SmbConstants.SMB_UNC_PATH, file.getUncPath());

        exchange.getMessage().setHeader(Exchange.FILE_NAME, file.getFileInformation().getNameInformation().toString());
    }

    @Override
    public File getFile() {
        return file;
    }

    public String getPath() {
        return getFile().getPath();
    }

    public String getUncPath() {
        return getFile().getUncPath();
    }

    public InputStream getInputStream() {
        return getFile().getInputStream();
    }

    public long getSize() {
        return file.getFileInformation().getStandardInformation().getEndOfFile();
    }

    @Override
    public Object getBody() {
        try (InputStream is = file.getInputStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
