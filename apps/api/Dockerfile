FROM node:24-alpine AS builder
WORKDIR /app
COPY package*.json ./
COPY tsconfig*.json ./
COPY prisma.config.ts ./
RUN npm ci
COPY . .
RUN DATABASE_URL="postgresql://x:x@localhost:5432/x" npx prisma generate
RUN npm run build
RUN ls -la dist/

FROM node:24-alpine AS production
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma
EXPOSE 3000
CMD ["node", "dist/main"]
