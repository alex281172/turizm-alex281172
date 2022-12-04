require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: localPattern.sc

require: weather.sc

require: functions.js

require: namesRu/names-ru.csv
    module = sys.zb-common
    name = Names
    var = Names
    
    
init:
    # создание переменной для переход на последний State с помощью postProcess. 
    # Можно использовать в state CatchAll
    bind("postProcess", function($context) {
        $context.session.lastState = $context.currentState;
        });
        
#создание глобальных конвертеров
init:
    if (!$global.$converters) {
        $global.$converters = {};
    }
    $global.$converters
        .NamesConverter = function(parseTree) {
            var id = parseTree.Names[0].value;
            return Names[id].value;
        };

  

theme: /
    state: Welcome
        q!: $regex</start>
        q!: $hi *

# вывод приветсвенной картинки
        script:
            $temp.hi = $parseTree._hi;
            $response.replies = $response.replies  || [];
            $response.replies.push( {
                type: "image",
                imageUrl: "https://fs.tonkosti.ru/95/7b/957bcf478b48kcggkoogg44g0.jpg",
                text: "Давай закажем погоду!"
                });
            $session.MychatId = $request.channelUserId;
# приветсвие с именем пользователя если оно сохранено ранее
        
        if: $client.name
            a: Здравствуйте, {{ $client.name }}!
            a: Ваш Id {{$session.MychatId}}
        
        else:
            random:
                a: Привет, привет!
                a: Здравствуйте!
                a: Ваш Id {{$session.MychatId}}
            
        a: Я бот туристической компании "{{ $injector.botName }}"
        # a: Ваш Id {{$session.MychatId}}
        go!: /SendMail_add/Email

# модуль предложения услуг по подбору тура
theme: /Service
    state: SuggestHelp
        a: Давайте я помогу Вам подобрать тур?
        buttons:
            "Да"
            "Нет"
            "Погода"
            "Регистрация"
# проверка наличия/отсутствия имени
        state: Accepted
            q: * (да/давай*/хорошо/ок*) *
            a: Отлично!
# если имя есть то переход на проверку имени
            if: $client.name
                go!: /ConfirmData/Confirm
# если имени нет то переход на полученеи имени
            else:
                a: Как вас зовут?
                go: /Anketing/MyNameAsk    
# если отказ от подбора тура, то предложение узнать погоду
        state: Rejected
            q: * (нет/не*) *
            a: Давайте помогу найти для вас хорошую погоду?
            buttons:
                "Да"
                "Нет"
# переход на поиск погоды если да, завершение если нет
            state: Accepted
                q: * (да/давай*/хорошо/ок*) *
                go!: /Weather/StartWeather            
            state: Rejected
                q: * (нет/не*) *
                a: Как жаль! У нас так много хорошей погоды для Вас...
                a: Если у Вас остались вопросы, то вы можете перезвонить по 8(812)000-00-00
              
# переход на поиск погоды в файле weather.sc 
        state: Weather
            q: $weather
            go!: /Weather/StartWeather

# переход получение данных о пользователе
        state: AskRegistration
            q: $registr
            a: Давайте познакомиcя. Как вас зовут?
            go: /Anketing/MyNameAsk 
        
            
            
# отработка несматченных данных
    state: CatchAll || noContext = true
        event!: noMatch
        a: Я Вас не понял
        # переход на последний State
        go!: {{ $context.session.lastState }} 


# подтверждения ранее введенных данных пользователя
theme: /ConfirmData
    state: Confirm
        a: Кажется мы знакомы, {{ $client.name }}.
        a: Ваш телефон {{ $client.phone }}?
        buttons:
            "Да"
            "Нет"
# переход на выбор погоды в случае подтверждения данных 
    state: Accepted
        q: * (да/давай*/хорошо/ок*) *
        a: Здорово общаться со старыми друзьями!
        a: Давайте выберем погоду и подберем тур
        go!: /Weather/StartWeather    
    
# переход на анкетирования если нет подтверждения 
    state: Rejected
        q: * (нет/не*) *
        a: Давайте познакомиcя. Как вас зовут?
        go: /Anketing/MyNameAsk



# анкетирование
theme: /Anketing
    state: MyNameAsk
# очистка переменных
        script:
                delete $client.city
                delete $client.quantity
                delete $client.children
                delete $client.date
                delete $client.duration
                delete $client.hotelstars
                delete $session.MailAnswer

# Проверка и стандартизация имена с помощью зависимости namesRu
        q: * $Name *
        script:
            $temp.name = $parseTree._Name.name  || $client.name;
        a: Ваше имя: {{$temp.name}}?
        script:
            $session.probablyName = $temp.name;
        buttons:
            "Да"
            "Нет"
# проверка правильности имени
# если правльно переход на ввод телефона
        state: Confirm
            q: * (да/прави*/ок*) *
            script:
                $client.name = $session.probablyName;

            a: Отлично! Нужен ваш телефон
            go!: /Phone/Ask
# если не правильно переход на ввод имени
        state: Rejected
            q: * (нет/не*) *
            a: Повторите ваше имя ->:
            go: /Anketing/MyNameAsk 
# если не имя переход на ввод имени
    state: MyWrong
        q: *
        a: Это не имя
        a: Повторите ваше имя ->:
        go: /Anketing/MyNameAsk  
       
# получение телефона пользователя
theme: /Phone
    state: Ask || modal = true
        random:
            a: Введите номер телефона по которому с Вами можно свзяться ->:
            a: Для продолжения диалога укажите ваш телефон в формате 79993332211 ->:
        buttons:
                "отмена"
# получение телефона по формату паттерна и переход на стейт Confirm
        state: Get
            q: $phone
            script:
                log("!!!!" + toPrettyString($parseTree));
            go!: /Phone/Confirm
# действия если телефона не по формату паттерна и переход на стейт Ask  
        state: Wrong
            q: *
            a: Укажите правильный номер телефона - >:
            go!: /Phone/Ask
# переход на стейт Welcomeесли отмена
        state: Rejected
            q: * (нет/не*/отмена) *
            go!: /Welcome
# модуль подтвеждения телефона
    state: Confirm
        script:
            $temp.phone = $parseTree._phone || $client.phone;
        a: Ваш номер {{ $temp.phone }}, верно?
        script:
            $session.probablyPhone = $temp.phone;
        buttons:
            "Да"
            "Нет"
# если верный телефон то переход на еще одну проверку
        state: Yes
            q: (да/верно)
            script:
                $client.phone = $session.probablyPhone;
                delete $session.probablyPhone;
            a: Хорошо. Давайте все проверим.
            a: Вас зовут {{$client.name}}, ваш телефон {{$client.phone}}.
            buttons:
                "Да"
                "Нет"
# переход на подбор погоды если все верно 
            state: Yes
                q: (да/верно)
                a: Давайте подберем тур
                go!: /Weather/StartWeather                
            state: No
                q: (нет/не верно)
                a: Повторим регистрацию
                a: Как Вас зовут?
                go: /Anketing/MyNameAsk                    
        state: No
            q: (нет/не верно)
            go!: /Phone/Ask

