# =============================================================================
# DOCKER COMPOSE MAKEFILE
# =============================================================================

# DOCKER_COMPOSE = docker-compose - команда для запуска Docker Compose.
# Используется единая переменная для consistency и возможной future-proofing
# (например, если нужно перейти на 'docker compose' вместо 'docker-compose')
DOCKER_COMPOSE = docker compose

# NEXUS_URL = http://localhost:8081 - адрес Nexus repository для health-проверки.
# Nexus - это артефакт-репозиторий для хранения билдов (аналог JFrog Artifactory)
NEXUS_URL = http://localhost:8081

# .PHONY - список «фантомных» целей (не файлы): all up start stop clean logs
# Эти цели не создают файлы с соответствующими именами, а выполняют действия
.PHONY: all up start stop clean logs build-artifacts rebuild

# =============================================================================
# CROSS-PLATFORM NEXUS HEALTH CHECK
# =============================================================================

# Кросс-платформенное ожидание готовности Nexus
# Определяем команду ожидания в зависимости от операционной системы

# Windows (OS=Windows_NT): PowerShell-цикл с 'Invoke-WebRequest'
# - Бесконечный цикл while($true)
# - Пытается выполнить HTTP-запрос к статусу Nexus
# - При успехе: break выходит из цикла
# - При ошибке: пишет сообщение и ждет 5 секунд
ifeq ($(OS),Windows_NT)
WAIT_CMD = powershell -Command "while ($$true) { \
		try { \
			Invoke-WebRequest -UseBasicParsing -Uri $(NEXUS_URL)/service/rest/v1/status -ErrorAction Stop; \
			break \
		} \
		catch { \
			Write-Host 'Nexus not ready, sleeping...'; \
			Start-Sleep -Seconds 5 \
		} \
	}"
else
# Linux/macOS: цикл until curl -sf...; do sleep 5; done
# - curl -s: silent (тихий режим)
# - curl -f: fail (завершается с ошибкой при HTTP error)
# - until: выполняет команду пока она не завершится успешно (exit code 0)
# - При неудаче: выводит сообщение и ждет 5 секунд
WAIT_CMD = until curl -sf $(NEXUS_URL)/service/rest/v1/status; do \
		echo 'Nexus not ready, sleeping...'; sleep 5; \
	done
endif

# =============================================================================
# MAIN TARGETS
# =============================================================================

# all: основная цель по умолчанию (запускается при вызове просто 'make')
# Обычно зависит от up, но здесь явно не определена, поэтому нужно добавить:
all: up build-artifacts start

# up: запуск всей инфраструктуры с ожиданием готовности Nexus
# 1. Запускает Nexus в detached mode (-d)
# 2. Ожидает пока Nexus станет здоровым (health check)
# 3. Выводит сообщение о успешном запуске
up:
	$(DOCKER_COMPOSE) up -d nexus
	@echo "Waiting for Nexus to be healthy..."
	@$(WAIT_CMD)
	@echo "Nexus is healthy!"

# build-artifacts: сборка persons-api сервиса без кэша
# --no-cache: принудительная пересборка без использования кэша Docker
# @ в начале: не выводит саму команду в консоль, только результат
build-artifacts:
	@$(DOCKER_COMPOSE) build persons-api --no-cache

# start: запуск всех сервисов в фоновом режиме
# -d: detached mode (в фоне)
start:
	$(DOCKER_COMPOSE) up -d

# stop: остановка всех сервисов
# down: останавливает и удаляет контейнеры, сети, образы и тома
stop:
	$(DOCKER_COMPOSE) down

# clean: полная очистка - остановка сервисов и удаление артефактов
# 1. Останавливает сервисы (stop)
# 2. Принудительно удаляет контейнеры (-f)
# 3. Удаляет dangling volumes (неиспользуемые тома)
# 4. Удаляет директорию сборки проекта
clean: stop
	$(DOCKER_COMPOSE) rm -f
	# Удаляет dangling volumes (2>/dev/null скрывает ошибки если томов нет)
	docker volume rm $$(docker volume ls -qf dangling=true) 2>/dev/null || true
	# Рекурсивно удаляет директорию сборки
	rm -rf ./person-service/build

# logs: просмотр логов в реальном времени
# -f: follow (следить за новыми сообщениями)
# --tail=200: показать последние 200 строк логов
logs:
	$(DOCKER_COMPOSE) logs -f --tail=200

# rebuild: полная пересборка - очистка и запуск заново
# Зависит от clean и all (up)
rebuild: clean all

# =============================================================================
# USAGE EXAMPLES:
# =============================================================================
# make              # Запуск всей инфраструктуры (all -> up)
# make up           # Запуск с ожиданием Nexus
# make start        # Быстрый запуск без ожидания
# make stop         # Остановка сервисов
# make clean        # Полная очистка
# make logs         # Просмотр логов
# make rebuild      # Полная пересборка
# make build-artifacts # Пересборка persons-api