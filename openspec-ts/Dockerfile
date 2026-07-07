FROM node:22-bookworm-slim

# Run as host user (avoids permission issues with mounted volumes).
# node:22 already ships a 'node' user, remap it to match the host UID/GID.
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN if [ "$GROUP_ID" != "1000" ]; then groupmod -g ${GROUP_ID} node; fi \
    && if [ "$USER_ID" != "1000" ]; then usermod -u ${USER_ID} node; fi \
    && chown -R node:node /home/node

WORKDIR /app
RUN mkdir -p /app/node_modules && chown -R node:node /app
USER node

EXPOSE 3000
CMD ["npm", "run", "start:dev"]



