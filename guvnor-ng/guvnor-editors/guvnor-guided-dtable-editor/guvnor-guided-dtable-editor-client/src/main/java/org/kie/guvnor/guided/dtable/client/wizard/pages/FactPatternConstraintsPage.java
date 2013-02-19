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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.kie.guvnor.commons.ui.client.widget.HumanReadableDataTypes;
import org.kie.guvnor.datamodel.oracle.DataType;
import org.kie.guvnor.guided.dtable.client.resources.i18n.Constants;
import org.kie.guvnor.guided.dtable.client.widget.DTCellValueWidgetFactory;
import org.kie.guvnor.guided.dtable.client.wizard.pages.events.ConditionsDefinedEvent;
import org.kie.guvnor.guided.dtable.client.wizard.pages.events.DuplicatePatternsEvent;
import org.kie.guvnor.guided.dtable.model.ConditionCol52;
import org.kie.guvnor.guided.dtable.model.DTCellValue52;
import org.kie.guvnor.guided.dtable.model.GuidedDecisionTable52;
import org.kie.guvnor.guided.dtable.model.Pattern52;
import org.kie.guvnor.guided.rule.model.BaseSingleFieldConstraint;

/**
 * A page for the guided Decision Table Wizard to define Fact Pattern
 * Constraints
 */
@Dependent
public class FactPatternConstraintsPage extends AbstractGuidedDecisionTableWizardPage
        implements
        FactPatternConstraintsPageView.Presenter {

    @Inject
    private FactPatternConstraintsPageView view;

    public String getTitle() {
        return Constants.INSTANCE.DecisionTableWizardFactPatternConstraints();
    }

    public void initialise() {
        if ( oracle == null ) {
            return;
        }
        view.init( this );
        view.setValidator( getValidator() );

        //Set-up a factory for value editors
        view.setDTCellValueWidgetFactory( DTCellValueWidgetFactory.getInstance( model,
                                                                                oracle,
                                                                                false,
                                                                                allowEmptyValues() ) );
        content.setWidget( view );
    }

    public void prepareView() {
        //Setup the available patterns, that could have changed each time this page is visited
        view.setAvailablePatterns( this.model.getPatterns() );
    }

    public boolean isComplete() {

        //Have all patterns conditions been defined?
        boolean areConditionsDefined = true;
        for ( Pattern52 p : model.getPatterns() ) {
            for ( ConditionCol52 c : p.getChildColumns() ) {
                if ( !getValidator().isConditionValid( c ) ) {
                    areConditionsDefined = false;
                    break;
                }
            }
        }

        //TODO Signal Condition definitions to other pages
        final ConditionsDefinedEvent event = new ConditionsDefinedEvent( areConditionsDefined );
        //eventBus.fireEventFromSource( event,
        //                              context );

        return areConditionsDefined;
    }

    public void onDuplicatePatterns( final @Observes DuplicatePatternsEvent event ) {
        view.setArePatternBindingsUnique( event.getArePatternBindingsUnique() );
    }

    public void onConditionsDefined( final @Observes ConditionsDefinedEvent event ) {
        view.setAreConditionsDefined( event.getAreConditionsDefined() );
    }

    public void selectPattern( final Pattern52 pattern ) {

        //Pattern is null when programmatically deselecting an item
        if ( pattern == null ) {
            return;
        }

        //Add Fact fields
        final String type = pattern.getFactType();
        final String[] fieldNames = oracle.getFieldCompletions( type );
        final List<AvailableField> availableFields = new ArrayList<AvailableField>();
        for ( String fieldName : fieldNames ) {
            final String fieldType = oracle.getFieldType( type,
                                                          fieldName );
            final String fieldDisplayType = HumanReadableDataTypes.getUserFriendlyTypeName( fieldType );
            final AvailableField field = new AvailableField( fieldName,
                                                             fieldType,
                                                             fieldDisplayType,
                                                             BaseSingleFieldConstraint.TYPE_LITERAL );
            availableFields.add( field );
        }

        //Add predicates
        if ( model.getTableFormat() == GuidedDecisionTable52.TableFormat.EXTENDED_ENTRY ) {
            final AvailableField field = new AvailableField( Constants.INSTANCE.DecisionTableWizardPredicate(),
                                                             BaseSingleFieldConstraint.TYPE_PREDICATE );
            availableFields.add( field );
        }

        view.setAvailableFields( availableFields );
        view.setChosenConditions( pattern.getChildColumns() );
    }

    public void setChosenConditions( final Pattern52 pattern,
                                     final List<ConditionCol52> conditions ) {
        pattern.getChildColumns().clear();
        pattern.getChildColumns().addAll( conditions );
    }

    public String[] getOperatorCompletions( final Pattern52 selectedPattern,
                                            final ConditionCol52 selectedCondition ) {

        final String factType = selectedPattern.getFactType();
        final String factField = selectedCondition.getFactField();
        final String[] ops = this.oracle.getOperatorCompletions( factType,
                                                                 factField );

        //Operators "in" and "not in" are only allowed if the Calculation Type is a Literal
        final List<String> filteredOps = new ArrayList<String>();
        for ( String op : ops ) {
            filteredOps.add( op );
        }
        if ( BaseSingleFieldConstraint.TYPE_LITERAL != selectedCondition.getConstraintValueType() ) {
            filteredOps.remove( "in" );
        }

        //But remove "in" if the Fact\Field is enumerated
        if ( oracle.hasEnums( factType,
                              factField ) ) {
            filteredOps.remove( "in" );
        }

        final String[] displayOps = new String[ filteredOps.size() ];
        filteredOps.toArray( displayOps );

        return displayOps;
    }

    public GuidedDecisionTable52.TableFormat getTableFormat() {
        return model.getTableFormat();
    }

    @Override
    public boolean hasEnum( final Pattern52 selectedPattern,
                            final ConditionCol52 selectedCondition ) {
        final String factType = selectedPattern.getFactType();
        final String factField = selectedCondition.getFactField();
        return oracle.hasEnums( factType,
                                factField );
    }

    @Override
    public boolean requiresValueList( final Pattern52 selectedPattern,
                                      final ConditionCol52 selectedCondition ) {
        //Don't show a Value List if either the Fact\Field is empty
        final String factType = selectedPattern.getFactType();
        final String factField = selectedCondition.getFactField();
        boolean enableValueList = !( ( factType == null || "".equals( factType ) ) || ( factField == null || "".equals( factField ) ) );

        //Don't show Value List if operator does not accept one
        if ( enableValueList ) {
            enableValueList = validator.doesOperatorAcceptValueList( selectedCondition );
        }

        //Don't show a Value List if the Fact\Field has an enumeration
        if ( enableValueList ) {
            enableValueList = !oracle.hasEnums( factType,
                                                factField );
        }
        return enableValueList;
    }

    @Override
    public void assertDefaultValue( final Pattern52 selectedPattern,
                                    final ConditionCol52 selectedCondition ) {
        final List<String> valueList = Arrays.asList( modelUtils.getValueList( selectedCondition ) );
        if ( valueList.size() > 0 ) {
            final String defaultValue = cellUtils.asString( selectedCondition.getDefaultValue() );
            if ( !valueList.contains( defaultValue ) ) {
                selectedCondition.getDefaultValue().clearValues();
            }
        } else {
            //Ensure the Default Value has been updated to represent the column's data-type.
            final DTCellValue52 defaultValue = selectedCondition.getDefaultValue();
            final DataType.DataTypes dataType = cellUtils.getDataType( selectedPattern,
                                                                       selectedCondition );
            cellUtils.assertDTCellValue( dataType,
                                         defaultValue );
        }

    }

}
