import dataCity from './data_city.json';
/**
const apiData = [{
  "appName": 'example-client',
  "dependcyApps": [{
    "appName": 'example-service';
    "services": [{
      "name": "com.quancheng.test.service.UserService",
      "callCount": 1908818
    }]
  }]
}]
*/
function getApiMock() {
  const dataLen = 25;
  const data = dataCity.RECORDS;
  data.length = dataLen;
  console.info(data);

  return data.map(function(item, index) {
    const dependcyApps = new Array(parseInt(Math.random() * 5));
    for (var i = 0; i < dependcyApps.length; i++) {
      const dependcyKey = parseInt(Math.random() * 100) % (dataLen - 1);
      dependcyApps[i] = {
        "appName": data[dependcyKey].name,
        "dependcyServices": [{
          "serviceName": "com.quancheng.test.service.UserService",
          "callCount": 1908818
        }]
      }
    }
    return {
      "appName": item.name,
      "dependcyApps": dependcyApps
    };
  })
}

export default getApiMock();
