/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.endpoint.ui.factory;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointDefinitionFactory;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.endpoint.ui.util.TemplateParameterContainer;

import javax.xml.namespace.QName;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class TemplateDefinitionFactory extends EndpointDefinitionFactory{
    public static final Log log = LogFactory.getLog(TemplateDefinitionFactory.class);
    TemplateParameterContainer instance;

    public TemplateDefinitionFactory(){
        instance = new TemplateParameterContainer();
    }

    public TemplateParameterContainer getParameterContainer(){
        return instance;
    }
    /**
     * Extracts the QoS information from the XML which represents a WSDL/Address/Default endpoints
     * for Template endpoints
     * @param elem XML which represents the endpoint with QoS information
     * @return the created endpoint definition
     */
    public EndpointDefinition createDefinition(OMElement elem) {
        EndpointDefinition definition = new EndpointDefinition();

        OMAttribute optimize
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "optimize"));
        OMAttribute encoding
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "encoding"));

        OMAttribute trace = elem.getAttribute(new QName(
                XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.TRACE_ATTRIB_NAME));
        if (trace != null && trace.getAttributeValue() != null) {
            String traceValue = trace.getAttributeValue();
            if (XMLConfigConstants.TRACE_ENABLE.equals(traceValue)) {
                definition.enableTracing();
                definition.enableStatistics(); // Tracing needs statistics to be enabled
            } else {
                definition.disableTracing();
            }
        }

        if (optimize != null && optimize.getAttributeValue().length() > 0) {
            String method = optimize.getAttributeValue().trim();
            if ("mtom".equalsIgnoreCase(method)) {
                definition.setUseMTOM(true);
            } else if ("swa".equalsIgnoreCase(method)) {
                definition.setUseSwa(true);
            } else{
                TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.optimize, method );
            }
        }

        if (encoding != null && encoding.getAttributeValue() != null) {
            definition.setCharSetEncoding(encoding.getAttributeValue());
        }else if(encoding != null) {
            TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.charSetEncoding, encoding.getAttributeValue());
        }

        OMElement wsAddr = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableAddressing"));
        if (wsAddr != null) {

            definition.setAddressingOn(true);

            OMAttribute version = wsAddr.getAttribute(new QName("version"));
            if (version != null && version.getAttributeValue() != null) {
                String versionValue = version.getAttributeValue().trim().toLowerCase();
                if (SynapseConstants.ADDRESSING_VERSION_FINAL.equals(versionValue) ||
                        SynapseConstants.ADDRESSING_VERSION_SUBMISSION.equals(versionValue)) {
                    definition.setAddressingVersion(version.getAttributeValue());
                    TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.addressingVersion, version.getAttributeValue());
                } else {
                    handleException("未知的地址版本. 地址版本的只能取值为 " +
                            " 'final' 活 'submission' .");
                }
            }

            String useSepList = wsAddr.getAttributeValue(new QName("separateListener"));
            if (useSepList != null) {
                if ("true".equals(useSepList.trim().toLowerCase())) {
                    definition.setUseSeparateListener(true);
                } else{
                    TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.separateListener,
                                                              useSepList.trim().toLowerCase() );
                }
            }
        }

        OMElement wsSec = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableSec"));
        if (wsSec != null) {

            definition.setSecurityOn(true);

            OMAttribute policyKey      = wsSec.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "policy"));
            OMAttribute inboundPolicyKey  = wsSec.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "inboundPolicy"));
            OMAttribute outboundPolicyKey = wsSec.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "outboundPolicy"));

            if (policyKey != null && policyKey.getAttributeValue() != null) {
                definition.setWsSecPolicyKey(policyKey.getAttributeValue());
                TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.policy, policyKey.getAttributeValue());
            } else {
                if (inboundPolicyKey != null && inboundPolicyKey.getAttributeValue() != null) {
                    definition.setInboundWsSecPolicyKey(inboundPolicyKey.getAttributeValue());
                    TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.inboundWsSecPolicyKey, inboundPolicyKey.getAttributeValue());
                }
                if (outboundPolicyKey != null && outboundPolicyKey.getAttributeValue() != null) {
                    definition.setOutboundWsSecPolicyKey(outboundPolicyKey.getAttributeValue());
                    TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.outboundWsSecPolicyKey, outboundPolicyKey.getAttributeValue());
                }
            }
        }

        // set the timeout configuration
        OMElement timeout = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "timeout"));
        if (timeout != null) {
            OMElement duration = timeout.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "duration"));

            if (duration != null) {
                String d = duration.getText();
                if (d != null) {
                    try {
                        //only set if not a template mapping
                        if (!TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.timeoutDuration, d)) {
                            Pattern pattern = Pattern.compile("\\{.*\\}");
                            if (pattern.matcher(d).matches()) {
                                d = d.trim().substring(1, d.length() - 1);
                                SynapseXPath xpath = new SynapseXPath(d);
                                definition.setDynamicTimeoutExpression(xpath);
                            } else {
                                long timeoutMilliSeconds = Long.parseLong(d.trim());
                                definition.setTimeoutDuration(timeoutMilliSeconds);
                            }
                        }
                    } catch (NumberFormatException e) {
                        handleException("端点超时持续时间应为数字，但不是数字");
                    }catch (JaxenException e) {
                        handleException("无法将动态端点超时分配为synapse表达式");
                    }
                }
            }

            OMElement action = timeout.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "responseAction"));
            if (action != null && action.getText() != null) {
                String actionString = action.getText();
                if ("discard".equalsIgnoreCase(actionString.trim())) {
                    definition.setTimeoutAction(SynapseConstants.DISCARD);
                } else if ("fault".equalsIgnoreCase(actionString.trim())) {
                    definition.setTimeoutAction(SynapseConstants.DISCARD_AND_FAULT);
                //if value is not even a template mapping handle as exception
                } else if(!TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.timeoutAction, actionString)) {
                    handleException("无效的超时操作, 操作 : "
                            + actionString + " 不被支持");
                }
            }
        }

        OMElement markAsTimedOut = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE,
            XMLConfigConstants.MARK_FOR_SUSPENSION));

        if (markAsTimedOut != null) {

            OMElement timeoutCodes = markAsTimedOut.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.ERROR_CODES));
            //only set if not a template mapping
            if (timeoutCodes != null && timeoutCodes.getText() != null &&
                    !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.timeoutErrorCodes, timeoutCodes.getText())) {
                StringTokenizer st = new StringTokenizer(timeoutCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addTimeoutErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("超时错误吗应该被指定 " +
                            "以逗号分隔的有效数字 : " + timeoutCodes.getText(), e);
                    }
                }
            }

            OMElement retriesBeforeSuspend = markAsTimedOut.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.RETRIES_BEFORE_SUSPENSION));
            //only set if not a template mapping
            if (retriesBeforeSuspend != null && retriesBeforeSuspend.getText() != null &&
                    !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.retriesOnTimeoutBeforeSuspend, retriesBeforeSuspend.getText())) {
                try {
                    definition.setRetriesOnTimeoutBeforeSuspend(
                        Integer.parseInt(retriesBeforeSuspend.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("挂起[超时]之前的重试次数应指定为有效数字 : " + retriesBeforeSuspend.getText(), e);
                }
            }

            OMElement retryDelay = markAsTimedOut.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.RETRY_DELAY));
            if (retryDelay != null && retryDelay.getText() != null &&
                    !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.retryDurationOnTimeout, retryDelay.getText())) {
                try {
                    definition.setRetryDurationOnTimeout(
                        Integer.parseInt(retryDelay.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("超时的重试延迟应指定为有效数字 : " + retryDelay.getText(), e);
                }
            }
        }

        // support backwards compatibility with Synapse 1.2 - for suspendDurationOnFailure
        OMElement suspendDurationOnFailure = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, "suspendDurationOnFailure"));
        if (suspendDurationOnFailure != null && suspendDurationOnFailure.getText() != null &&
            !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.initialSuspendDuration, suspendDurationOnFailure.getText().trim())
            ) {

            log.warn("Configuration uses deprecated style for endpoint 'suspendDurationOnFailure'");
            try {
                definition.setInitialSuspendDuration(
                        1000 * Long.parseLong(suspendDurationOnFailure.getText().trim()));
                definition.setSuspendProgressionFactor((float) 1.0);
            } catch (NumberFormatException e) {
                handleException("初始挂起持续时间应指定为有效数字 : " + suspendDurationOnFailure.getText(), e);
            }
        }

        OMElement suspendOnFailure = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE,
            XMLConfigConstants.SUSPEND_ON_FAILURE));

        if (suspendOnFailure != null) {

            OMElement suspendCodes = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.ERROR_CODES));
            if (suspendCodes != null && suspendCodes.getText() != null &&
                !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.suspendErrorCodes, suspendCodes.getText())) {

                StringTokenizer st = new StringTokenizer(suspendCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addSuspendErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("应将挂起错误代码指定为用逗号分隔的有效数字 : " + suspendCodes.getText(), e);
                    }
                }
            }

            OMElement initialDuration = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.SUSPEND_INITIAL_DURATION));
            if (initialDuration != null && initialDuration.getText() != null &&
                !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.initialSuspendDuration, initialDuration.getText())) {
                try {
                    definition.setInitialSuspendDuration(
                        Integer.parseInt(initialDuration.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("初始挂起持续时间应指定为有效数字 : " + initialDuration.getText(), e);
                }
            }

            OMElement progressionFactor = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.SUSPEND_PROGRESSION_FACTOR));
            if (progressionFactor != null && progressionFactor.getText() != null &&
                    !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.suspendProgressionFactor, progressionFactor.getText())) {
                try {
                    definition.setSuspendProgressionFactor(
                        Float.parseFloat(progressionFactor.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("应将挂起持续时间累进系数指定为有效的浮点数 : " + progressionFactor.getText(), e);
                }
            }

            OMElement maximumDuration = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.SUSPEND_MAXIMUM_DURATION));
            if (maximumDuration != null && maximumDuration.getText() != null &&
                    !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.suspendMaximumDuration, maximumDuration.getText()) ) {
                try {
                    definition.setSuspendMaximumDuration(
                        Long.parseLong(maximumDuration.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("最大挂起持续时间应指定为有效数字 : " + maximumDuration.getText(), e);
                }
            }
        }

        OMElement retryConfig = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, XMLConfigConstants.RETRY_CONFIG));

        if (retryConfig != null) {

            OMElement retryDisabledErrorCodes = retryConfig.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE, "disabledErrorCodes"));
            if (retryDisabledErrorCodes != null && retryDisabledErrorCodes.getText() != null &&
                    !TemplateMappingsPopulator.populateMapping(instance, TemplateParameterContainer.EndpointDefKey.retryDisabledErrorCodes, retryDisabledErrorCodes.getText())) {

                StringTokenizer st = new StringTokenizer(
                        retryDisabledErrorCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addRetryDisabledErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("应将挂起错误代码指定为用逗号分隔的有效数字 : "
                                + retryDisabledErrorCodes.getText(), e);
                    }
                }
            }
        }

        return definition;
    }

    protected static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    protected static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
