patterns:
    $hi = (привет*/здравствуй*)
    $phone = $regexp<79\d{9}>
    $weather = (*погода*)
    $registr = * (*регист*) *
    $quantity = $regexp<^[0-9]|не знаю|отмена>
    $tourdate = $regexp<^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\d\d|не знаю|отмена>
    $tourduration = $regexp<^[1-9][0-9]|[1-9]|не знаю|отмена>
    $hotelstars = (2/3/4/5|не знаю|отмена)
    $note = *
    $confirmnote = (да|ок|отмена|изменить)
    # без использования конвертера
    # $Name = $entity<Names>
    # с импользованием конвертера который в NamesRu.sc
    $Name = $entity<Names> || converter = $converters.NamesConverter


