// Copyright 2013-2016 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.gradle.tasks.download;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.gradle.api.tasks.TaskExecutionException;
import org.junit.Test;

/**
 * Test the {@link VerifyAction}
 * @author Michel Kraemer
 */
public class VerifyTest extends TestBase {
    /**
     * Makes a verify task
     * @param downloadTask a configured download task to depend on
     * @return the unconfigured verify task
     */
    private Verify makeVerifyTask(Download downloadTask) {
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("type", Verify.class);
        Verify v = (Verify)downloadTask.getProject().task(taskParams, "verifyFile");
        v.dependsOn(downloadTask);
        return v;
    }
    
    /**
     * Tests if the Verify task can verify a file using its MD5 checksum
     * @throws Exception if anything goes wrong
     */
    @Test
    public void verifyMD5() throws Exception {
        Download t = makeProjectAndTask();
        t.src(makeSrc(TEST_FILE_NAME));
        File dst = folder.newFile();
        t.dest(dst);
        
        Verify v = makeVerifyTask(t);
        v.algorithm("MD5");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(contents);
        String calculatedChecksum = Hex.encodeHexString(md5.digest());
        v.checksum(calculatedChecksum);
        v.src(t.getDest());
        
        t.execute();
        v.execute(); // will throw if the checksum is not OK
    }
    
    /**
     * Tests if the Verify task fails if the checksum is wrong
     * @throws Exception if anything goes wrong
     */
    @Test(expected = TaskExecutionException.class)
    public void verifyWrongMD5() throws Exception {
        Download t = makeProjectAndTask();
        t.src(makeSrc(TEST_FILE_NAME));
        File dst = folder.newFile();
        t.dest(dst);
        
        Verify v = makeVerifyTask(t);
        v.algorithm("MD5");
        v.checksum("WRONG");
        v.src(t.getDest());
        
        t.execute();
        v.execute(); // should throw
    }
}
