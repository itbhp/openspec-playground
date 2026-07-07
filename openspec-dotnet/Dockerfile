FROM mcr.microsoft.com/dotnet/sdk:8.0

# Run as host user (avoids permission issues with mounted volumes)
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN groupadd -g ${GROUP_ID} appuser 2>/dev/null || true \
    && useradd -u ${USER_ID} -g ${GROUP_ID} -m -s /bin/bash appuser 2>/dev/null || true

WORKDIR /app
RUN chown -R appuser:appuser /app

# Warm up the NuGet cache (packages live in ~/.nuget, not shadowed by the volume)
COPY --chown=appuser:appuser EmployeeApi.csproj ./
USER appuser
RUN dotnet restore

# Polling watcher is needed for file changes on bind mounts
ENV DOTNET_USE_POLLING_FILE_WATCHER=1
ENV ASPNETCORE_URLS=http://0.0.0.0:5000

EXPOSE 5000
CMD ["dotnet", "watch", "run", "--no-launch-profile", "--urls", "http://0.0.0.0:5000"]

