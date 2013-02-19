/*
 * Copyright 2011 JBoss Inc
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
package org.kie.guvnor.guided.dtable.client.wizard.pages;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.guvnor.guided.dtable.client.resources.i18n.Constants;
import org.kie.guvnor.guided.dtable.client.wizard.util.NewGuidedDecisionTableAssetWizardContext;

/**
 * A summary page for the guided Decision Table Wizard
 */
@Dependent
public class SummaryPage extends AbstractGuidedDecisionTableWizardPage
        implements
        SummaryPageView.Presenter {

    @Inject
    private SummaryPageView view;

    public String getTitle() {
        return Constants.INSTANCE.DecisionTableWizardSummary();
    }

    public boolean isComplete() {
        String assetName = view.getBaseFileName();
        boolean isValid = ( assetName != null && !assetName.equals( "" ) );
        view.setHasInvalidAssetName( !isValid );
        return isValid;
    }

    public void initialise() {
        view.init( this );
        view.setBaseFileName( context.getBaseFileName() );
        view.setContextPath( context.getContextPath() );
        view.setTableFormat( ( (NewGuidedDecisionTableAssetWizardContext) context ).getTableFormat() );
        content.setWidget( view );
    }

    public void prepareView() {
        //Nothing required
    }

}
