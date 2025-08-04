# Setting Up Secrets for Plugin Signing and Publishing

This guide explains how to generate and configure the necessary secrets for signing and publishing the IntelliJ plugin to the JetBrains Marketplace.

## Prerequisites

- JetBrains account with plugin developer access
- OpenSSL installed on your system
- Access to your forked repository settings (for GitHub Actions)

## Required Secrets

1. **CERTIFICATE_CHAIN** - Your plugin signing certificate chain
2. **PRIVATE_KEY** - Your private key for signing
3. **PRIVATE_KEY_PASSWORD** - Password for the private key
4. **PUBLISH_TOKEN** - JetBrains Marketplace API token

## Step 1: Generate Plugin Signing Certificate

### Option A: Self-Signed Certificate (for development/testing)

```bash
# Generate private key
openssl genrsa -aes256 -out private.pem 4096

# Generate certificate signing request
openssl req -new -key private.pem -out request.csr

# Generate self-signed certificate (valid for 1 year)
openssl x509 -req -days 365 -in request.csr -signkey private.pem -out certificate.crt

# Create certificate chain file
cat certificate.crt > chain.crt
```

### Option B: Purchased Certificate (for production)

1. Purchase a code signing certificate from a trusted CA (e.g., DigiCert, Sectigo)
2. Follow the CA's instructions to generate a CSR and obtain the certificate
3. Download the certificate chain (usually includes intermediate certificates)

## Step 2: Prepare Secrets

```bash
# Export the private key (remove password if needed for automation)
openssl rsa -in private.pem -out private-nopass.pem

# Verify the certificate chain
openssl verify -CAfile chain.crt certificate.crt
```

## Step 3: Create JetBrains Marketplace Token

1. Log in to [JetBrains Hub](https://hub.jetbrains.com/)
2. Navigate to your profile → **Authentication**
3. Click **New token**
4. Name it (e.g., "IntelliJ Plugin Publishing")
5. Add scope: **Marketplace** → **Plugin upload**
6. Copy the generated token

## Step 4: Set Up Secrets for Local Development

### Using Environment Variables

```bash
# Add to your shell profile (.bashrc, .zshrc, etc.)
export CERTIFICATE_CHAIN=$(cat chain.crt | base64 -w 0)
export PRIVATE_KEY=$(cat private-nopass.pem | base64 -w 0)
export PRIVATE_KEY_PASSWORD="<YOUR_PRIVATE_KEY_PASSWORD>"
export PUBLISH_TOKEN="<YOUR_JETBRAINS_MARKETPLACE_TOKEN>"
```

## Step 5: Set Up GitHub Actions Secrets

1. Go to your repository on GitHub
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Add the following repository secrets:
   - `CERTIFICATE_CHAIN`: Contents of chain.crt
   - `PRIVATE_KEY`: Contents of private-nopass.pem
   - `PRIVATE_KEY_PASSWORD`: Your private key password
   - `PUBLISH_TOKEN`: Your JetBrains Marketplace token

## Step 6: Testing

### Test Signing Locally

```bash
# Using Earthly
earthly +sign --version=1.0.0

# Using Gradle directly
./gradlew signPlugin
```

### Test Publishing (Dry Run)

```bash
# Using Earthly
earthly +publish --version=1.0.0

# Using Gradle directly
./gradlew publishPlugin --dry-run
```