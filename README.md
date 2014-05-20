org.kholupko.xoredtest.ui
=========================

Composite Launch Configuration UI PlugIn

Принцип работы простой: На основоной вкладке есть фильтруемое дерево существующих конфигураций, при выборе(отметке) конфигурация попадает в таблицу, в которой дополнительно выбирается режим запуска (из поддерживаемых).

Что я не сделал, но, для полноценного опубликованного плагина, должен был бы:
1) Тесты. Но, как я понимаю, для тестирования ui нужен какой-нибудь дополнительный инструмент, типа вашего Q7, а для core нужно делать кучу всяких mock'ов, в общем, т.к. это все-таки тестовое задание и я уже потратил кучу времени, я не стал.
2) Help
Что можно добавить/улучшить, т.е., что бы я доделал в следующих версиях:
1) Возможность указывать дополнительные условия кроме режима запуска, например, дождаться ли завершения одной конфигурации, прежде чем запускть другую, соответственно порядок запуска и т.д.
2) Добавить тулбар к дереву с кнопками фильрации по определенным типам, закрытым проектам и.т.п., как это сделано в дереве создания конфигураций
3) Добавить возможность запуска в режиме profile и кастомных режимах
