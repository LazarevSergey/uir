# uir
УИР
разработка функций в рамках класса utis/Interface.java
разрабатывал на основе следующего примера из базы, однако пытался не разрабатывать как для частного случая(дорабатываю)
<окно 
  имя: "Help frame" 
  меню :<менюбар конт:[ 
    <меню имя:"Файл" конт:[ 
      <элменю имя:"Открыть.." команда: (@ отобразить ( выч ( `Супер Фрейм` . "схема" ) ) @)> 
      <элменю имя:"Добавить.." команда: (@ поменять "1" "1" поменять "2" "1" @)> 
      <элменю имя:"Выход.." команда: (@ выход @)> 
    ]> 
    <меню имя:"Операции" конт:[ 
      <меню имя:"Создать схему" конт:[ 
        <элменю имя:"Концепт схему" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
        <элменю имя:"Группу" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
      ]> 
      <элменю имя:"Удалить схему" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
      <элменю имя:"Latex в концепт" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
      <меню имя:"Экспорт" конт:[ 
        <элменю имя:"В текстовый файл" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
        <элменю имя:"В latex" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
      ]> 
      <меню имя:"Скомпилировать" конт:[ 
        <элменю имя:"Все" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
      ]> 
      <элменю имя:"Поиск.." команда: (@ поменять "1" "1" поменять "2" "1" @)> 
    ]> 
    <меню имя:"Настройки" конт:[ 
      <меню имя:"Отображаемое название" конт:[ 
        <радбат ид:"Отображаемое название" конт:"Схема"> 
        <радбат ид:"Отображаемое название" конт:"Краткое заглавие"> 
      ]> 
      <меню имя:"Отображаемые аннотации" конт:[ 
        (@ применить (lx.<радбат набор: "аннотации" конт:%x%> l) ( запрос "SELECT name FROM `objects` WHERE type='аннотация'" ) @) 
      ]> 
      <меню имя:"Отображаемая прагма" конт:[ 
        (@ применить (lx.<радбат набор: "прагма" конт:%x%> l) ( запрос "SELECT name FROM `objects` WHERE type='прагма'" ) @) 
      ]> 
    ]> 
    <меню имя:"Справка" конт:[ 
      <элменю имя:"Справка" команда: (@ поменять "1" "1" поменять "2" "1" @)> 
    ]> 
  ]> 
  размер:(300 ; 300 ; 300 ; 300) 
  конт:[ 
    <сплитпанель ориентация:"горизонтальный" середина: "0.5" конт:[ 
      <скрол место:"Left" конт:[ 
        <древопомощи> 
      ]> 
      <скрол место:"Right" конт:[ 
        <html ид:"справка"> 
      ]> 
   ]> 
  ]>
