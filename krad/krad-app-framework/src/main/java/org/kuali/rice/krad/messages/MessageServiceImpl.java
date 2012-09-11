package org.kuali.rice.krad.messages;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the {@link MessageService} that allows {@link MessageProvider} implementations
 * to be configured for exposing external message repositories
 *
 * <p>
 * This message service implementation essentially delegates all calls down to one or more message
 * providers. When more than one message provider is configured, providers higher up in the chain will
 * receive priority. That is, when finding a message the first provider that has the message will be used
 * and no others will be consulted. When finding a collection of messages, if the same message (key) exists
 * from more than one provider, the message from the first encountered provider (in the List) will
 * be used.
 * </p>
 *
 * <p>
 * The default namespace and component are constants of the service implementation and may not be changed.
 * </p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class MessageServiceImpl implements MessageService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MessageServiceImpl.class);
    
    protected static final String DEFAULT_NAMESPACE_CODE = "KUALI";
    protected static final String DEFAULT_COMPONENT_CODE = "All";

    private List<MessageProvider> messageProviders;

    /**
     * @see MessageService#getMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    public Message getMessage(String namespace, String component, String name) {
        return getMessage(namespace, component, name, null);
    }

    /**
     * @see MessageService#getMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    public Message getMessage(String namespace, String component, String name, String locale) {
        Message message = null;

        // use default namespace and component if not given
        if (StringUtils.isBlank(namespace)) {
            namespace = DEFAULT_NAMESPACE_CODE;
        }

        if (StringUtils.isBlank(component)) {
            namespace = DEFAULT_COMPONENT_CODE;
        }

        for (MessageProvider provider : messageProviders) {
            message = provider.getMessage(namespace, component, name, locale);

            if (message != null) {
                // don't check with any additional providers
                break;
            }
        }

        return message;
    }

    /**
     * @see MessageService#getMessageText(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getMessageText(String namespace, String component, String name) {
        return getMessageText(namespace, component, name, null);
    }

    /**
     * @see MessageService#getMessageText(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getMessageText(String namespace, String component, String name, String locale) {
        Message message = getMessage(namespace, component, name, locale);
        if (message != null) {
            return message.getText();
        }

        return null;
    }

    /**
     * @see MessageService#getMessageText(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getMessageText(String name) {
        return getMessageText(name, null);
    }

    /**
     * @see MessageService#getMessageText(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getMessageText(String name, String locale) {
        Message message = getMessage(null, null, name, locale);
        if (message != null) {
            return message.getText();
        }

        return null;
    }

    /**
     * @see MessageService#getAllMessagesForComponent(java.lang.String, java.lang.String)
     */
    public Collection<Message> getAllMessagesForComponent(String namespace, String component) {
        return getAllMessagesForComponent(namespace, component, null);
    }

    /**
     * @see MessageService#getAllMessagesForComponent(java.lang.String, java.lang.String)
     */
    public Collection<Message> getAllMessagesForComponent(String namespace, String component, String locale) {
        Collection<Message> messages = new ArrayList<Message>();

        for (MessageProvider provider : messageProviders) {
            Collection<Message> providerMessages = provider.getAllMessagesForComponent(namespace, component, locale);
            mergeMessages(messages, providerMessages);
        }

        return messages;
    }

    /**
     * Merges the second collection into the first collection
     *
     * <p>
     * If a message with the same key (namespace, component, and name) is found in both collections, the message
     * from first collection will remain. That is, the message in the second collection will NOT override
     * </p>
     *
     * @param messages - collection to be merged into
     * @param messagesToMerge - collection that will be merged with first
     */
    protected void mergeMessages(Collection<Message> messages, Collection<Message> messagesToMerge) {
        for (Message message : messagesToMerge) {
            if (!messages.contains(message)) {
                messages.add(message);
            }
        }
    }

    /**
     * Retrieves the collection of message providers configured with the message service
     *
     * @return List<MessageProvider> message provider implementations
     */
    protected List<MessageProvider> getMessageProviders() {
        return messageProviders;
    }

    /**
     * Setter for the collection of message providers that should be used by the message service
     * implementation
     *
     * @param messageProviders
     */
    public void setMessageProviders(List<MessageProvider> messageProviders) {
        this.messageProviders = messageProviders;
    }
}
