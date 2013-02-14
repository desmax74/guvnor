/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.globals.backend.server.util;

import java.util.List;

import org.kie.guvnor.globals.model.Global;
import org.kie.guvnor.globals.model.GlobalsModel;
import org.kie.guvnor.services.config.model.imports.Imports;
import org.kie.guvnor.services.config.model.imports.ImportsParser;

/**
 * This class persists the rule model to DRL and back
 */
public class GlobalsPersistence {

    private static final GlobalsPersistence INSTANCE = new GlobalsPersistence();

    protected GlobalsPersistence() {
    }

    public static GlobalsPersistence getInstance() {
        return INSTANCE;
    }

    public String marshal( final GlobalsModel model ) {
        final StringBuilder sb = new StringBuilder();
        sb.append( model.getImports().toString() );
        sb.append( "\n" );
        for ( Global global : model.getGlobals() ) {
            sb.append( "global " ).append( global.getClassName() ).append( " " ).append( global.getAlias() ).append( ";\n" );
        }
        return sb.toString();
    }

    public GlobalsModel unmarshal( final String content ) {
        final Imports imports = ImportsParser.parseImports( content );
        final List<Global> globals = GlobalsParser.parseGlobals( content );
        final GlobalsModel model = new GlobalsModel();
        model.setImports( imports );
        model.setGlobals( globals );
        return model;
    }

}
