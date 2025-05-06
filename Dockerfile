FROM rabbitmq:4.1.0-management

# 1. Install curl to download the plugin
RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y curl \
 && rm -rf /var/lib/apt/lists/*

# 2. Download delayed‐message plugin v3.8.17 (supports RabbitMQ ≥3.8.16)
ARG PLUGIN_VERSION=4.1.0
RUN curl -fsSL \
    https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v${PLUGIN_VERSION}/rabbitmq_delayed_message_exchange-${PLUGIN_VERSION}.ez \
    -o $RABBITMQ_HOME/plugins/rabbitmq_delayed_message_exchange-${PLUGIN_VERSION}.ez

# 3. Enable the plugin offline so it's active on first start
RUN rabbitmq-plugins enable rabbitmq_delayed_message_exchange