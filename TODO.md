TODO
====

##Паук
 - Убрать теги из распарсеной страницы
 - Убрать javascript из страницы
 - Оптимизировать скорость работы
 - Ввести единый regexp для картинок и ссылок: src, link, href etc
 - Переписать поиск ключевых слов

##Поиск
 - Ввести новые способы ранжирования
 - Возможно ввести классификаторы на основе МЛ
 - Один раз был зафиксирован баг с разной выдачей для запросов в верзнем и нижнем регистре. Повторить
 - Поиск изображений

##Redis
 - Протестировать на очень больших данных (текущие объемы - 400к ключевых слов, 6к страниц. Скорость - удоволетворительная)
 - Перевести проект на использование более новой библиотеки
 - Изменить значения на Hash

##Всё
 - Убрать все deprecated классы