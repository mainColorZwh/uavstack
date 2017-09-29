/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.uav.collect.client.copylogagent;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractPartitionJob;

public class TailFileTaskJob extends AbstractPartitionJob {

    private static final long serialVersionUID = 1L;

    protected ISystemLogger log;

    private static ThreadLocal<CopyOfProcessOfLogagent> cpy = new ThreadLocal<>();

    public TailFileTaskJob(ISystemLogger logger) {
        this.log = logger;
    }

    public void setCurrenttfref(CopyOfProcessOfLogagent c) {

        cpy.set(c);
    }

    public CopyOfProcessOfLogagent getCurrenttfref() {

        return cpy.get();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void work() {

        Map<TailFile, List<Map>> serverlogs = null;
        try {
            TailFile tf = (TailFile) this.get("tfevent");
            this.setCurrenttfref((CopyOfProcessOfLogagent) this.get("tfref"));
            // tfref = ((TaildirLogComponent) this.get("tfref"));
            serverlogs = this.getCurrenttfref().tailFileProcessSeprate(tf, true);

            for (Entry<TailFile, List<Map>> applogs : serverlogs.entrySet()) {
                if (log.isDebugEnable()) {
                    log.debug(this, "### Logvalue ###: " + applogs.getValue());
                }
            }

            if (!(serverlogs.isEmpty())) {
                this.getCurrenttfref().sendLogDataBatch(serverlogs);
            }
            else {
                if (log.isDebugEnable()) {
                    log.debug(this, "serverlogs is emptry!!!");
                }
            }

        }
        catch (Throwable t) {
            log.err(this, "Unable to tail files.", t);
        }
        finally {
            if (null != serverlogs) {
                serverlogs.clear();
            }
        }
    }

}
