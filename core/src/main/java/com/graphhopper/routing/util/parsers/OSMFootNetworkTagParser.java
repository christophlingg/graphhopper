/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.RouteNetwork;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;

import static com.graphhopper.routing.util.EncodingManager.getKey;

public class OSMFootNetworkTagParser implements RelationTagParser {
    private final EnumEncodedValue<RouteNetwork> footRouteEnc;
    // used for internal transformation from relations into footRouteEnc
    private final EnumEncodedValue<RouteNetwork> transformerRouteRelEnc = new EnumEncodedValue<>(getKey("foot", "route_relation"), RouteNetwork.class);

    public OSMFootNetworkTagParser(EnumEncodedValue<RouteNetwork> footRouteEnc, EncodedValue.InitializerConfig relConfig) {
        this.footRouteEnc = footRouteEnc;
        this.transformerRouteRelEnc.init(relConfig);
    }

    @Override
    public void handleRelationTags(IntsRef relFlags, ReaderRelation relation) {
        RouteNetwork oldFootNetwork = transformerRouteRelEnc.getEnum(false, relFlags);
        if (relation.hasTag("route", "hiking") || relation.hasTag("route", "foot")) {
            String tag = Helper.toLowerCase(relation.getTag("network", ""));
            RouteNetwork newFootNetwork = RouteNetwork.LOCAL;
            if ("lwn".equals(tag)) {
                newFootNetwork = RouteNetwork.LOCAL;
            } else if ("rwn".equals(tag)) {
                newFootNetwork = RouteNetwork.REGIONAL;
            } else if ("nwn".equals(tag)) {
                newFootNetwork = RouteNetwork.NATIONAL;
            } else if ("iwn".equals(tag)) {
                newFootNetwork = RouteNetwork.INTERNATIONAL;
            }
            if (oldFootNetwork == RouteNetwork.MISSING || oldFootNetwork.ordinal() > newFootNetwork.ordinal())
                transformerRouteRelEnc.setEnum(false, relFlags, newFootNetwork);
        }
    }

    @Override
    public void handleWayTags(IntsRef edgeFlags, ReaderWay way, IntsRef relationFlags) {
        // just copy value into different bit range
        RouteNetwork footNetwork = transformerRouteRelEnc.getEnum(false, relationFlags);
        footRouteEnc.setEnum(false, edgeFlags, footNetwork);
    }
}
