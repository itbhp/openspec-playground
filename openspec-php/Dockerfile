FROM dunglas/frankenphp:1.12-php8.5-trixie

ARG USER=appuser

# Install system dependencies
RUN apt-get update && apt-get install -y \
    unzip \
    git \
    libzip-dev \
    libicu-dev \
    && docker-php-ext-install zip intl pdo pdo_mysql \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Install Composer
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer

# Run as host user (avoids permission issues with mounted volumes)
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN groupadd -g ${GROUP_ID} ${USER} && useradd -u ${USER_ID} -g ${GROUP_ID} -m -s /bin/bash ${USER}

RUN setcap CAP_NET_BIND_SERVICE=+eip /usr/local/bin/frankenphp

RUN chown -R ${USER}:${USER} /config/caddy /data/caddy

WORKDIR /app

# FrankenPHP in classic mode (no worker)
ENV SERVER_NAME=":80"

COPY Caddyfile /etc/frankenphp/Caddyfile

USER ${USER}


