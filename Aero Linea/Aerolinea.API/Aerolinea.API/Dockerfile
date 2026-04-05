#Build 
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

COPY *.csproj ./
RUN dotnet restore

COPY . ./
RUN dotnet publish -c Release -o /app/publish

# Runtime 
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app

RUN apt-get update && apt-get install -y \
    libgdiplus \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libfontconfig1 \
    libfreetype6 \
    libssl-dev \
    fonts-liberation \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

COPY --from=build /app/publish .
COPY --from=build /src/native ./native

ENV LD_LIBRARY_PATH=/app/native/linux-x64:$LD_LIBRARY_PATH

ENTRYPOINT ["dotnet", "Aerolinea.API.dll"]