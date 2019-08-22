/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.monitoring.store;

import fish.payara.monitoring.collect.MonitoringDataCollector;

/**
 * A {@link MonitoringDataCollector} that is used as an adapter to {@link MonitoringDataSink} abstraction.
 *
 * @author Jan Bernitt
 */
public class SinkDataCollector implements MonitoringDataCollector {

    private static final char TAG_SEPARATOR = ' ';
    private static final char TAG_ASSIGN = '=';

    private final MonitoringDataSink sink;
    private final StringBuilder tags;

    public SinkDataCollector(MonitoringDataSink sink) {
        this(sink, new StringBuilder());
    }

    public SinkDataCollector(MonitoringDataSink sink, StringBuilder fullKey) {
        this.sink = sink;
        this.tags = fullKey;
    }

    @Override
    public MonitoringDataCollector collect(CharSequence key, long value) {
        int length = tags.length();
        if (tags.length() > 0) {
            tags.append(TAG_SEPARATOR);
        }
        tags.append(key);
        accept(value);
        tags.setLength(length);
        return this;
    }

    @Override
    public MonitoringDataCollector tag(CharSequence name, CharSequence value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        StringBuilder tagged = new StringBuilder(tags);
        int nameIndex = indexOf(name);
        if (nameIndex >= 0) {
            tagged.setLength(nameIndex);
        } else if (tagged.length() > 0) {
            tagged.append(TAG_SEPARATOR);
        }
        tagged.append(name).append(TAG_ASSIGN);
        appendEscaped(value, tagged);
        return new SinkDataCollector(sink, tagged);
    }

    /**
     * Makes sure that tag separating characters in values are replaced with underscore.
     */
    private static void appendEscaped(CharSequence value, StringBuilder tagged) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != ',' && c != ';' && c != ' ') {
                tagged.append(c);
            } else {
                tagged.append('_');
            }
        }
    }

    private int indexOf(CharSequence name) {
        String tag = name.toString();
        if (tags.indexOf(tag + TAG_ASSIGN) == 0) {
            return 0;
        }
        int idx = tags.indexOf(TAG_SEPARATOR + tag + TAG_ASSIGN);
        return idx < 0 ? idx : idx + 1;
    }

    private void accept(long value) {
        sink.accept(tags, value);
    }
}
