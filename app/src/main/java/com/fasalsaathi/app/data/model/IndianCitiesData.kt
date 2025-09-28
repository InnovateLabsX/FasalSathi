package com.fasalsaathi.app.data.model

data class IndianCity(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val district: String = ""
)

object IndianCitiesData {
    
    val cities = listOf(
        // Andhra Pradesh
        IndianCity("Visakhapatnam", "Andhra Pradesh", 17.6868, 83.2185, "Visakhapatnam"),
        IndianCity("Vijayawada", "Andhra Pradesh", 16.5062, 80.6480, "Krishna"),
        IndianCity("Guntur", "Andhra Pradesh", 16.3067, 80.4365, "Guntur"),
        IndianCity("Nellore", "Andhra Pradesh", 14.4426, 79.9865, "Nellore"),
        IndianCity("Kurnool", "Andhra Pradesh", 15.8281, 78.0373, "Kurnool"),
        IndianCity("Rajahmundry", "Andhra Pradesh", 17.0005, 81.8040, "East Godavari"),
        IndianCity("Tirupati", "Andhra Pradesh", 13.6288, 79.4192, "Chittoor"),
        
        // Arunachal Pradesh
        IndianCity("Itanagar", "Arunachal Pradesh", 27.0844, 93.6053, "Papum Pare"),
        IndianCity("Naharlagun", "Arunachal Pradesh", 27.1000, 93.7000, "Papum Pare"),
        
        // Assam
        IndianCity("Guwahati", "Assam", 26.1445, 91.7362, "Kamrup"),
        IndianCity("Silchar", "Assam", 24.8333, 92.7789, "Cachar"),
        IndianCity("Dibrugarh", "Assam", 27.4728, 94.9120, "Dibrugarh"),
        IndianCity("Jorhat", "Assam", 26.7509, 94.2037, "Jorhat"),
        IndianCity("Nagaon", "Assam", 26.3440, 92.6789, "Nagaon"),
        
        // Bihar
        IndianCity("Patna", "Bihar", 25.5941, 85.1376, "Patna"),
        IndianCity("Gaya", "Bihar", 24.7914, 84.9787, "Gaya"),
        IndianCity("Bhagalpur", "Bihar", 25.2425, 86.9842, "Bhagalpur"),
        IndianCity("Muzaffarpur", "Bihar", 26.1197, 85.3910, "Muzaffarpur"),
        IndianCity("Darbhanga", "Bihar", 26.1542, 85.8918, "Darbhanga"),
        IndianCity("Bihar Sharif", "Bihar", 25.1979, 85.5240, "Nalanda"),
        
        // Chhattisgarh
        IndianCity("Raipur", "Chhattisgarh", 21.2514, 81.6296, "Raipur"),
        IndianCity("Bhilai", "Chhattisgarh", 21.1938, 81.3509, "Durg"),
        IndianCity("Korba", "Chhattisgarh", 22.3595, 82.7501, "Korba"),
        IndianCity("Bilaspur", "Chhattisgarh", 22.0797, 82.1391, "Bilaspur"),
        
        // Goa
        IndianCity("Panaji", "Goa", 15.4909, 73.8278, "North Goa"),
        IndianCity("Margao", "Goa", 15.2993, 73.9626, "South Goa"),
        IndianCity("Vasco da Gama", "Goa", 15.3955, 73.8313, "South Goa"),
        
        // Gujarat
        IndianCity("Ahmedabad", "Gujarat", 23.0225, 72.5714, "Ahmedabad"),
        IndianCity("Surat", "Gujarat", 21.1702, 72.8311, "Surat"),
        IndianCity("Vadodara", "Gujarat", 22.3072, 73.1812, "Vadodara"),
        IndianCity("Rajkot", "Gujarat", 22.3039, 70.8022, "Rajkot"),
        IndianCity("Bhavnagar", "Gujarat", 21.7645, 72.1519, "Bhavnagar"),
        IndianCity("Jamnagar", "Gujarat", 22.4707, 70.0577, "Jamnagar"),
        IndianCity("Gandhinagar", "Gujarat", 23.2156, 72.6369, "Gandhinagar"),
        
        // Haryana
        IndianCity("Gurugram", "Haryana", 28.4595, 77.0266, "Gurugram"),
        IndianCity("Faridabad", "Haryana", 28.4089, 77.3178, "Faridabad"),
        IndianCity("Panipat", "Haryana", 29.3909, 76.9635, "Panipat"),
        IndianCity("Ambala", "Haryana", 30.3752, 76.7821, "Ambala"),
        IndianCity("Yamunanagar", "Haryana", 30.1290, 77.2674, "Yamunanagar"),
        IndianCity("Rohtak", "Haryana", 28.8955, 76.6066, "Rohtak"),
        IndianCity("Hisar", "Haryana", 29.1492, 75.7217, "Hisar"),
        
        // Himachal Pradesh
        IndianCity("Shimla", "Himachal Pradesh", 31.1048, 77.1734, "Shimla"),
        IndianCity("Dharamshala", "Himachal Pradesh", 32.2190, 76.3234, "Kangra"),
        IndianCity("Solan", "Himachal Pradesh", 30.9045, 77.0967, "Solan"),
        IndianCity("Mandi", "Himachal Pradesh", 31.7084, 76.9319, "Mandi"),
        
        // Jharkhand
        IndianCity("Ranchi", "Jharkhand", 23.3441, 85.3096, "Ranchi"),
        IndianCity("Jamshedpur", "Jharkhand", 22.8046, 86.2029, "East Singhbhum"),
        IndianCity("Dhanbad", "Jharkhand", 23.7957, 86.4304, "Dhanbad"),
        IndianCity("Bokaro", "Jharkhand", 23.6693, 85.9606, "Bokaro"),
        
        // Karnataka
        IndianCity("Bengaluru", "Karnataka", 12.9716, 77.5946, "Bengaluru Urban"),
        IndianCity("Mysuru", "Karnataka", 12.2958, 76.6394, "Mysuru"),
        IndianCity("Hubli", "Karnataka", 15.3647, 75.1240, "Dharwad"),
        IndianCity("Mangaluru", "Karnataka", 12.9141, 74.8560, "Dakshina Kannada"),
        IndianCity("Belgaum", "Karnataka", 15.8497, 74.4977, "Belagavi"),
        IndianCity("Gulbarga", "Karnataka", 17.3297, 76.8343, "Kalaburagi"),
        IndianCity("Davangere", "Karnataka", 14.4644, 75.9932, "Davangere"),
        
        // Kerala
        IndianCity("Thiruvananthapuram", "Kerala", 8.5241, 76.9366, "Thiruvananthapuram"),
        IndianCity("Kochi", "Kerala", 9.9312, 76.2673, "Ernakulam"),
        IndianCity("Kozhikode", "Kerala", 11.2588, 75.7804, "Kozhikode"),
        IndianCity("Thrissur", "Kerala", 10.5276, 76.2144, "Thrissur"),
        IndianCity("Kollam", "Kerala", 8.8932, 76.6141, "Kollam"),
        IndianCity("Kannur", "Kerala", 11.8745, 75.3704, "Kannur"),
        
        // Madhya Pradesh
        IndianCity("Bhopal", "Madhya Pradesh", 23.2599, 77.4126, "Bhopal"),
        IndianCity("Indore", "Madhya Pradesh", 22.7196, 75.8577, "Indore"),
        IndianCity("Gwalior", "Madhya Pradesh", 26.2183, 78.1828, "Gwalior"),
        IndianCity("Jabalpur", "Madhya Pradesh", 23.1815, 79.9864, "Jabalpur"),
        IndianCity("Ujjain", "Madhya Pradesh", 23.1765, 75.7885, "Ujjain"),
        IndianCity("Sagar", "Madhya Pradesh", 23.8388, 78.7378, "Sagar"),
        
        // Maharashtra
        IndianCity("Mumbai", "Maharashtra", 19.0760, 72.8777, "Mumbai City"),
        IndianCity("Pune", "Maharashtra", 18.5204, 73.8567, "Pune"),
        IndianCity("Nagpur", "Maharashtra", 21.1458, 79.0882, "Nagpur"),
        IndianCity("Nashik", "Maharashtra", 19.9975, 73.7898, "Nashik"),
        IndianCity("Aurangabad", "Maharashtra", 19.8762, 75.3433, "Aurangabad"),
        IndianCity("Solapur", "Maharashtra", 17.6599, 75.9064, "Solapur"),
        IndianCity("Amravati", "Maharashtra", 20.9374, 77.7796, "Amravati"),
        IndianCity("Kolhapur", "Maharashtra", 16.7050, 74.2433, "Kolhapur"),
        
        // Manipur
        IndianCity("Imphal", "Manipur", 24.8170, 93.9368, "Imphal West"),
        
        // Meghalaya
        IndianCity("Shillong", "Meghalaya", 25.5788, 91.8933, "East Khasi Hills"),
        
        // Mizoram
        IndianCity("Aizawl", "Mizoram", 23.7271, 92.7176, "Aizawl"),
        
        // Nagaland
        IndianCity("Kohima", "Nagaland", 25.6751, 94.1086, "Kohima"),
        IndianCity("Dimapur", "Nagaland", 25.9044, 93.7267, "Dimapur"),
        
        // Odisha
        IndianCity("Bhubaneswar", "Odisha", 20.2961, 85.8245, "Khurda"),
        IndianCity("Cuttack", "Odisha", 20.4625, 85.8828, "Cuttack"),
        IndianCity("Rourkela", "Odisha", 22.2604, 84.8536, "Sundargarh"),
        IndianCity("Berhampur", "Odisha", 19.3149, 84.7941, "Ganjam"),
        
        // Punjab
        IndianCity("Ludhiana", "Punjab", 30.9010, 75.8573, "Ludhiana"),
        IndianCity("Amritsar", "Punjab", 31.6340, 74.8723, "Amritsar"),
        IndianCity("Jalandhar", "Punjab", 31.3260, 75.5762, "Jalandhar"),
        IndianCity("Patiala", "Punjab", 30.3398, 76.3869, "Patiala"),
        IndianCity("Bathinda", "Punjab", 30.2084, 74.9519, "Bathinda"),
        IndianCity("Chandigarh", "Punjab", 30.7333, 76.7794, "Chandigarh"),
        
        // Rajasthan
        IndianCity("Jaipur", "Rajasthan", 26.9124, 75.7873, "Jaipur"),
        IndianCity("Jodhpur", "Rajasthan", 26.2389, 73.0243, "Jodhpur"),
        IndianCity("Kota", "Rajasthan", 25.2138, 75.8648, "Kota"),
        IndianCity("Bikaner", "Rajasthan", 28.0229, 73.3119, "Bikaner"),
        IndianCity("Udaipur", "Rajasthan", 24.5854, 73.7125, "Udaipur"),
        IndianCity("Ajmer", "Rajasthan", 26.4499, 74.6399, "Ajmer"),
        
        // Sikkim
        IndianCity("Gangtok", "Sikkim", 27.3389, 88.6065, "East Sikkim"),
        
        // Tamil Nadu
        IndianCity("Chennai", "Tamil Nadu", 13.0827, 80.2707, "Chennai"),
        IndianCity("Coimbatore", "Tamil Nadu", 11.0168, 76.9558, "Coimbatore"),
        IndianCity("Madurai", "Tamil Nadu", 9.9252, 78.1198, "Madurai"),
        IndianCity("Tiruchirappalli", "Tamil Nadu", 10.7905, 78.7047, "Tiruchirappalli"),
        IndianCity("Salem", "Tamil Nadu", 11.6643, 78.1460, "Salem"),
        IndianCity("Tirunelveli", "Tamil Nadu", 8.7139, 77.7567, "Tirunelveli"),
        IndianCity("Vellore", "Tamil Nadu", 12.9165, 79.1325, "Vellore"),
        
        // Telangana
        IndianCity("Hyderabad", "Telangana", 17.3850, 78.4867, "Hyderabad"),
        IndianCity("Warangal", "Telangana", 17.9689, 79.5941, "Warangal Urban"),
        IndianCity("Nizamabad", "Telangana", 18.6725, 78.0941, "Nizamabad"),
        IndianCity("Khammam", "Telangana", 17.2473, 80.1514, "Khammam"),
        
        // Tripura
        IndianCity("Agartala", "Tripura", 23.8315, 91.2868, "West Tripura"),
        
        // Uttar Pradesh
        IndianCity("Lucknow", "Uttar Pradesh", 26.8467, 80.9462, "Lucknow"),
        IndianCity("Kanpur", "Uttar Pradesh", 26.4499, 80.3319, "Kanpur Nagar"),
        IndianCity("Ghaziabad", "Uttar Pradesh", 28.6692, 77.4538, "Ghaziabad"),
        IndianCity("Agra", "Uttar Pradesh", 27.1767, 78.0081, "Agra"),
        IndianCity("Varanasi", "Uttar Pradesh", 25.3176, 82.9739, "Varanasi"),
        IndianCity("Meerut", "Uttar Pradesh", 28.9845, 77.7064, "Meerut"),
        IndianCity("Allahabad", "Uttar Pradesh", 25.4358, 81.8463, "Prayagraj"),
        IndianCity("Bareilly", "Uttar Pradesh", 28.3670, 79.4304, "Bareilly"),
        IndianCity("Aligarh", "Uttar Pradesh", 27.8974, 78.0880, "Aligarh"),
        IndianCity("Moradabad", "Uttar Pradesh", 28.8386, 78.7733, "Moradabad"),
        
        // Uttarakhand
        IndianCity("Dehradun", "Uttarakhand", 30.3165, 78.0322, "Dehradun"),
        IndianCity("Haridwar", "Uttarakhand", 29.9457, 78.1642, "Haridwar"),
        IndianCity("Roorkee", "Uttarakhand", 29.8543, 77.8880, "Haridwar"),
        IndianCity("Haldwani", "Uttarakhand", 29.2183, 79.5130, "Nainital"),
        
        // West Bengal
        IndianCity("Kolkata", "West Bengal", 22.5726, 88.3639, "Kolkata"),
        IndianCity("Howrah", "West Bengal", 22.5958, 88.2636, "Howrah"),
        IndianCity("Durgapur", "West Bengal", 23.4820, 87.3119, "Paschim Bardhaman"),
        IndianCity("Asansol", "West Bengal", 23.6739, 86.9524, "Paschim Bardhaman"),
        IndianCity("Siliguri", "West Bengal", 26.7271, 88.3953, "Darjeeling"),
        
        // Delhi (NCT)
        IndianCity("New Delhi", "Delhi", 28.6139, 77.2090, "New Delhi"),
        IndianCity("Delhi", "Delhi", 28.7041, 77.1025, "Delhi"),
        
        // Union Territories
        IndianCity("Puducherry", "Puducherry", 11.9416, 79.8083, "Puducherry"),
        IndianCity("Port Blair", "Andaman and Nicobar Islands", 11.6234, 92.7265, "South Andaman"),
        IndianCity("Kavaratti", "Lakshadweep", 10.5669, 72.6420, "Lakshadweep"),
        IndianCity("Daman", "Dadra and Nagar Haveli and Daman and Diu", 20.3974, 72.8328, "Daman"),
        IndianCity("Silvassa", "Dadra and Nagar Haveli and Daman and Diu", 20.2738, 73.0140, "Dadra and Nagar Haveli"),
        IndianCity("Jammu", "Jammu and Kashmir", 32.7266, 74.8570, "Jammu"),
        IndianCity("Srinagar", "Jammu and Kashmir", 34.0837, 74.7973, "Srinagar"),
        IndianCity("Leh", "Ladakh", 34.1526, 77.5771, "Leh")
    )
    
    fun getCitiesByState(state: String): List<IndianCity> {
        return cities.filter { it.state == state }
    }
    
    fun getAllStates(): List<String> {
        return cities.map { it.state }.distinct().sorted()
    }
    
    fun searchCities(query: String): List<IndianCity> {
        return cities.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.district.contains(query, ignoreCase = true) ||
            it.state.contains(query, ignoreCase = true)
        }
    }
    
    fun getCityByName(cityName: String, stateName: String? = null): IndianCity? {
        return if (stateName != null) {
            cities.find { it.name.equals(cityName, ignoreCase = true) && it.state.equals(stateName, ignoreCase = true) }
        } else {
            cities.find { it.name.equals(cityName, ignoreCase = true) }
        }
    }
}