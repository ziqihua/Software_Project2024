var mongoose = require('mongoose');
require('dotenv').config({ path: '../.env' });

mongoose.set('useNewUrlParser', true);
mongoose.set('useFindAndModify', false);
mongoose.set('useCreateIndex', true);
mongoose.set('useUnifiedTopology', true);


var connection_string = process.env.MONGODB_URI;
if (!connection_string) {
	throw new Error('MONGODB_URI is not defined in the environment variables.');
}
console.log("connection_string: ", connection_string)
mongoose.connect(connection_string);

var Schema = mongoose.Schema;

var donationSchema = new Schema({
	contributor: String,
	fund: String,
	date: Date,
	amount: Number
});

var fundSchema = new Schema({
	name: String,
	description: String,
	target: {type: Number, default: 0},
	org: String,
	donations: [donationSchema]
});

var organizationSchema = new Schema({
	login: {type: String, required: true, unique: true},
	password: String,
	name: String,
	description: String,
	funds: [fundSchema]
});

var contributorSchema = new Schema({
	login: {type: String, required: true, unique: true},
	password: String,
	name: String,
	email: String,
	creditCardNumber: String,
	creditCardCVV: String,
	creditCardExpiryMonth: {type: Number, default: 0},
	creditCardExpiryYear: {type: Number, default: 0},
	creditCardPostCode: String,
	donations: [donationSchema]
});


const donationModel = mongoose.model('Donation', donationSchema);
const fundModel = mongoose.model('Fund', fundSchema);
const organizationModel = mongoose.model('Organization', organizationSchema);
const contributorModel = mongoose.model('Contributor', contributorSchema);

module.exports = {
	Donation : donationModel,
	Fund : fundModel,
	Organization: organizationModel,
	Contributor: contributorModel }
