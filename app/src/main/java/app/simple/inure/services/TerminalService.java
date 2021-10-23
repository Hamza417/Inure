/*
 * Copyright (C) 2013 The Android Open Source Project
 *
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
 */

package app.simple.inure.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.SparseArray;

import app.simple.inure.decorations.terminal.Terminal;

/**
 * Background service that keeps {@link Terminal} instances running and warm
 * when UI isn't present.
 */
public class TerminalService extends Service {
    private final SparseArray <Terminal> mTerminals = new SparseArray <>();
    
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }
    
    public SparseArray <Terminal> getTerminals() {
        return mTerminals;
    }
    
    public int createTerminal() {
        // If our first terminal, start ourselves as long-lived service
        if (mTerminals.size() == 0) {
            startService(new Intent(this, TerminalService.class));
        }
        
        final Terminal term = new Terminal();
        term.start();
        mTerminals.put(term.key, term);
        return term.key;
    }
    
    public void destroyTerminal(int key) {
        final Terminal term = mTerminals.get(key);
        term.destroy();
        mTerminals.delete(key);
        
        // If our last terminal, tear down long-lived service
        if (mTerminals.size() == 0) {
            stopService(new Intent(this, TerminalService.class));
        }
    }
    
    public class ServiceBinder extends Binder {
        public TerminalService getService() {
            return TerminalService.this;
        }
    }
}